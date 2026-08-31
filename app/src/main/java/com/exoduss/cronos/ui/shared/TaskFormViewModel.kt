package com.exoduss.cronos.ui.shared

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.net.Uri
import com.exoduss.cronos.data.media.TaskMediaStore
import com.exoduss.cronos.data.repository.PreferencesRepository
import com.exoduss.cronos.data.repository.SubtaskRepository
import com.exoduss.cronos.data.repository.TaskMediaRepository
import com.exoduss.cronos.data.repository.TaskRepository
import com.exoduss.cronos.data.repository.TaskTypeRepository
import com.exoduss.cronos.domain.model.*
import com.exoduss.cronos.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject

data class SubtaskFormItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val isDone: Boolean = false
)

data class TaskFormState(
    val editingId: String? = null,
    val title: String = "",
    val description: String = "",
    val typeId: String? = null,
    val priority: Priority = Priority.MEDIUM,
    val dueDate: LocalDate? = null,
    val dueTime: LocalTime? = null,
    val recurrence: Recurrence = Recurrence.NONE,
    val pinned: Boolean = false,
    val reminderEnabled: Boolean = false,
    val reminderFrequency: Int = 1,         // 1–12 lembretes por dia
    val reminderDaysBefore: Int = 0,        // quantos dias antes começar os lembretes
    // Capturados no momento em que o formulário de edição é aberto,
    // evitando race condition com mudanças concorrentes de status
    val originalStatus: TaskStatus = TaskStatus.PENDING,
    val originalCreatedAt: Long = 0L,
    val originalNextSpawned: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TaskFormViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val taskTypeRepository: TaskTypeRepository,
    private val subtaskRepository: SubtaskRepository,
    private val preferencesRepository: PreferencesRepository,
    private val taskMediaRepository: TaskMediaRepository,
    private val reminderScheduler: ReminderScheduler,
    private val mediaStore: TaskMediaStore
) : ViewModel() {

    // Tipos disponíveis
    val taskTypes: StateFlow<List<TaskType>> = taskTypeRepository.getAllTypes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val favoriteTypeId: StateFlow<String?> = preferencesRepository.favoriteTypeId
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // ── Form Sheet ─────────────────────────────────────────
    private val _showFormSheet = MutableStateFlow(false)
    val showFormSheet = _showFormSheet.asStateFlow()

    private val _formState = MutableStateFlow(TaskFormState())
    val formState = _formState.asStateFlow()

    private val _formSubtasks = MutableStateFlow<List<SubtaskFormItem>>(emptyList())
    val formSubtasks = _formSubtasks.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    private val _saveError = MutableStateFlow<String?>(null)
    val saveError = _saveError.asStateFlow()

    // ── Detail Sheet ───────────────────────────────────────
    private val _showDetailSheet = MutableStateFlow(false)
    val showDetailSheet = _showDetailSheet.asStateFlow()

    private val _selectedTask = MutableStateFlow<Task?>(null)
    val selectedTask = _selectedTask.asStateFlow()

    val selectedTaskSubtasks: StateFlow<List<Subtask>> = _selectedTask
        .flatMapLatest { task ->
            if (task != null) subtaskRepository.getSubtasksForTask(task.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val selectedTaskType: StateFlow<TaskType?> = combine(_selectedTask, taskTypes) { task, types ->
        task?.typeId?.let { id -> types.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // ── Media ──────────────────────────────────────────────
    // taskId da tarefa que acabou de ser concluída, aguardando prompt de mídia
    private val _pendingMediaTaskId = MutableStateFlow<String?>(null)
    val pendingMediaTaskId = _pendingMediaTaskId.asStateFlow()

    val selectedTaskMedia: StateFlow<List<TaskMedia>> = _selectedTask
        .flatMapLatest { task ->
            if (task != null) taskMediaRepository.getMediaForTask(task.id)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ── Form operations ────────────────────────────────────

    fun openAddForm(preselectedDate: LocalDate? = null) {
        _formState.value = TaskFormState(
            typeId = favoriteTypeId.value,
            dueDate = preselectedDate
        )
        _formSubtasks.value = emptyList()
        _showFormSheet.value = true
    }

    fun openEditForm(task: Task) {
        viewModelScope.launch {
            // Load subtasks before opening the sheet — prevents saving with an empty list
            // if the user taps Save before the async fetch would have completed.
            val subs = subtaskRepository.getSubtasksForTask(task.id).first()
            _formSubtasks.value = subs.map { SubtaskFormItem(it.id, it.title, it.isDone) }
            _formState.value = TaskFormState(
                editingId           = task.id,
                title               = task.title,
                description         = task.description,
                typeId              = task.typeId,
                priority            = task.priority,
                dueDate             = task.dueDate,
                dueTime             = task.dueTime,
                recurrence          = task.recurrence,
                pinned              = task.pinned,
                reminderEnabled     = task.reminderEnabled,
                reminderFrequency   = task.reminderFrequency,
                reminderDaysBefore  = task.reminderDaysBefore,
                originalStatus      = task.status,
                originalCreatedAt   = task.createdAt,
                originalNextSpawned = task.nextSpawned
            )
            _showFormSheet.value = true
        }
    }

    fun closeForm() {
        _showFormSheet.value = false
        _formState.value = TaskFormState()
        _formSubtasks.value = emptyList()
        _saveError.value = null
    }

    fun clearSaveError() { _saveError.value = null }

    fun updateTitle(v: String) = _formState.update { it.copy(title = v) }
    fun updateDescription(v: String) = _formState.update { it.copy(description = v) }
    fun updateTypeId(v: String?) = _formState.update { it.copy(typeId = v) }
    fun updatePriority(v: Priority) = _formState.update { it.copy(priority = v) }
    fun updateDueDate(v: LocalDate?) = _formState.update {
        it.copy(dueDate = v, reminderEnabled = if (v == null) false else it.reminderEnabled)
    }
    fun updateDueTime(v: LocalTime?) = _formState.update { it.copy(dueTime = v) }
    fun updateRecurrence(v: Recurrence) = _formState.update { it.copy(recurrence = v) }
    fun updatePinned(v: Boolean) = _formState.update { it.copy(pinned = v) }
    fun updateReminder(v: Boolean) = _formState.update { it.copy(reminderEnabled = v) }
    fun updateReminderFrequency(v: Int) = _formState.update { it.copy(reminderFrequency = v.coerceIn(1, 12)) }
    fun updateReminderDaysBefore(v: Int) = _formState.update { it.copy(reminderDaysBefore = v) }

    fun addSubtask(title: String) {
        if (title.isBlank()) return
        _formSubtasks.update { it + SubtaskFormItem(title = title) }
    }

    fun removeSubtask(id: String) = _formSubtasks.update { list -> list.filter { it.id != id } }

    fun toggleFormSubtask(id: String) = _formSubtasks.update { list ->
        list.map { if (it.id == id) it.copy(isDone = !it.isDone) else it }
    }

    fun saveTask() {
        val s = _formState.value
        if (s.title.isBlank()) return

        viewModelScope.launch {
            _isSaving.value = true
            val result = runCatching {
                // Usa os valores capturados no openEditForm — sem query ao banco (sem race condition)
                val task = Task(
                    id          = s.editingId ?: UUID.randomUUID().toString(),
                    title       = s.title.trim(),
                    description = s.description.trim(),
                    typeId      = s.typeId,
                    priority    = s.priority,
                    status      = if (s.editingId != null) s.originalStatus else TaskStatus.PENDING,
                    dueDate     = s.dueDate,
                    dueTime     = s.dueTime,
                    recurrence  = s.recurrence,
                    pinned      = s.pinned,
                    reminderEnabled    = s.reminderEnabled,
                    reminderFrequency  = s.reminderFrequency,
                    reminderDaysBefore = s.reminderDaysBefore,
                    nextSpawned = if (s.editingId != null) s.originalNextSpawned else false,
                    createdAt  = if (s.editingId != null) s.originalCreatedAt else System.currentTimeMillis(),
                    updatedAt  = System.currentTimeMillis()
                )
                val subtasks = _formSubtasks.value
                    .filter { it.title.isNotBlank() }
                    .mapIndexed { idx, item ->
                        Subtask(
                            id = item.id, taskId = task.id,
                            title = item.title, isDone = item.isDone, order = idx
                        )
                    }
                // Operação atômica: tarefa + subtarefas em uma única transação Room
                taskRepository.saveTaskWithSubtasks(task, subtasks)
                // Alarmes fora da transação (chamada ao sistema, não ao banco)
                reminderScheduler.scheduleReminders(task)
            }
            _isSaving.value = false
            result
                .onSuccess { closeForm() }
                .onFailure { _saveError.value = "Erro ao salvar tarefa. Tente novamente." }
        }
    }

    // ── Detail operations ──────────────────────────────────

    suspend fun getTaskById(id: String): Task? = taskRepository.getTaskById(id)

    fun openDetail(task: Task) {
        _selectedTask.value = task
        _showDetailSheet.value = true
    }

    fun closeDetail() {
        _showDetailSheet.value = false
        _selectedTask.value = null
    }

    fun updateTaskStatus(taskId: String, status: TaskStatus) {
        viewModelScope.launch {
            // Busca a tarefa ANTES de atualizar — fonte autoritativa de recorrência/dueDate,
            // independente da tela que disparou a mudança.
            val task = taskRepository.getTaskById(taskId)
            val previousStatus = task?.status
            taskRepository.updateStatus(taskId, status)
            _selectedTask.update { if (it?.id == taskId) it.copy(status = status) else it }
            // Cancela lembretes quando tarefa é concluída ou cancelada
            if (status == TaskStatus.DONE || status == TaskStatus.CANCELLED) {
                reminderScheduler.cancelReminders(taskId)
            }
            // Materializa a próxima ocorrência de tarefas recorrentes ao concluir pela 1ª vez.
            // O guard !task.nextSpawned evita duplicatas ao desmarcar/marcar concluída de novo.
            if (status == TaskStatus.DONE && previousStatus != TaskStatus.DONE && task != null && !task.nextSpawned) {
                task.nextOccurrence()?.let { next ->
                    val subs = subtaskRepository.getSubtasksForTask(task.id).first()
                        .map { it.copy(id = UUID.randomUUID().toString(), taskId = next.id, isDone = false) }
                    taskRepository.saveTaskWithSubtasks(next, subs)
                    reminderScheduler.scheduleReminders(next)
                    taskRepository.markNextSpawned(task.id)
                }
            }
            // Aciona prompt de mídia ao concluir pela primeira vez
            if (status == TaskStatus.DONE && previousStatus != TaskStatus.DONE) {
                _pendingMediaTaskId.value = taskId
            }
        }
    }

    fun clearPendingMediaPrompt() { _pendingMediaTaskId.value = null }

    fun addMedia(taskId: String, uri: String, mediaType: String) {
        viewModelScope.launch {
            // Copia para armazenamento interno (permanente); em falha, mantém o URI original.
            val storedUri = runCatching { mediaStore.importFromUri(Uri.parse(uri), mediaType) }
                .getOrDefault(uri)
            taskMediaRepository.addMedia(
                TaskMedia(
                    id = UUID.randomUUID().toString(),
                    taskId = taskId,
                    uri = storedUri,
                    mediaType = mediaType,
                    createdAt = System.currentTimeMillis(),
                    pendingBackup = true
                )
            )
        }
    }

    fun deleteMedia(media: TaskMedia) {
        viewModelScope.launch {
            taskMediaRepository.deleteMedia(media)
            mediaStore.deleteFile(media.uri)
        }
    }

    fun toggleSubtaskInDetail(id: String, isDone: Boolean) {
        viewModelScope.launch { subtaskRepository.toggleSubtask(id, isDone) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            reminderScheduler.cancelReminders(task.id)
            subtaskRepository.deleteSubtasksForTask(task.id)
            // Remove mídias (arquivos internos + linhas) — antes ficavam órfãs no disco/DB
            val mediaList = taskMediaRepository.getMediaForTask(task.id).first()
            mediaList.forEach { mediaStore.deleteFile(it.uri) }
            taskMediaRepository.deleteForTask(task.id)
            taskRepository.deleteTask(task)
            closeDetail()
        }
    }

    fun togglePin(task: Task) {
        viewModelScope.launch { taskRepository.togglePin(task.id, !task.pinned) }
    }

    fun editFromDetail() {
        val task = _selectedTask.value ?: return
        closeDetail()
        openEditForm(task)
    }
}
