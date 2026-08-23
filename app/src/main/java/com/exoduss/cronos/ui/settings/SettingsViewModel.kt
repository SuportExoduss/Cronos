package com.exoduss.cronos.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exoduss.cronos.data.repository.BackupRepository
import com.exoduss.cronos.data.repository.PreferencesRepository
import com.exoduss.cronos.data.repository.TaskRepository
import com.exoduss.cronos.data.repository.TaskTypeRepository
import com.exoduss.cronos.domain.model.Task
import com.exoduss.cronos.domain.model.TaskStatus
import com.exoduss.cronos.domain.model.TaskType
import com.exoduss.cronos.notifications.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class TaskStats(
    val total: Int = 0,
    val pending: Int = 0,
    val done: Int = 0,
    val completionRate: Int = 0
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: PreferencesRepository,
    private val taskTypeRepository: TaskTypeRepository,
    private val reminderScheduler: ReminderScheduler,
    private val backupRepository: BackupRepository,
    taskRepository: TaskRepository
) : ViewModel() {

    val userName: StateFlow<String> = preferencesRepository.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val darkMode: StateFlow<Boolean> = preferencesRepository.darkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val useSystemTheme: StateFlow<Boolean> = preferencesRepository.useSystemTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val dailySummaryEnabled: StateFlow<Boolean> = preferencesRepository.dailySummaryEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val dailySummaryHour: StateFlow<Int> = preferencesRepository.dailySummaryHour
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 8)

    val taskTypes: StateFlow<List<TaskType>> = taskTypeRepository.getAllTypes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val backupMediaEnabled: StateFlow<Boolean> = preferencesRepository.backupMediaEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val stats: StateFlow<TaskStats> = taskRepository.getAllTasks()
        .map { tasks ->
            val total = tasks.size
            val done = tasks.count { it.status == TaskStatus.DONE }
            val pending = tasks.count { it.status == TaskStatus.PENDING || it.status == TaskStatus.IN_PROGRESS }
            val rate = if (total > 0) (done * 100 / total) else 0
            TaskStats(total = total, pending = pending, done = done, completionRate = rate)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TaskStats())

    fun updateUserName(name: String) {
        viewModelScope.launch { preferencesRepository.setUserName(name) }
    }

    fun setDarkMode(dark: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setUseSystemTheme(false)
            preferencesRepository.setDarkMode(dark)
        }
    }

    fun setUseSystemTheme(use: Boolean) {
        viewModelScope.launch { preferencesRepository.setUseSystemTheme(use) }
    }

    fun setDailySummaryEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDailySummaryEnabled(enabled)
            val hour = preferencesRepository.dailySummaryHour.first()
            if (enabled) reminderScheduler.scheduleDailySummary(hour)
            else reminderScheduler.cancelDailySummary()
        }
    }

    fun setDailySummaryHour(hour: Int) {
        viewModelScope.launch {
            preferencesRepository.setDailySummaryHour(hour)
            val enabled = preferencesRepository.dailySummaryEnabled.first()
            if (enabled) reminderScheduler.scheduleDailySummary(hour)
        }
    }

    fun setBackupMediaEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setBackupMediaEnabled(enabled) }
    }

    // ── Backup local (export/import via SAF) ──────────────────────────────
    // Mesma base que o backup do Google Drive vai reutilizar (apenas troca o destino).

    private val _backupMessage = MutableStateFlow<String?>(null)
    val backupMessage: StateFlow<String?> = _backupMessage.asStateFlow()

    fun clearBackupMessage() { _backupMessage.value = null }

    /** Exporta todos os dados para o arquivo escolhido pelo usuário. */
    fun exportTo(uri: Uri) {
        viewModelScope.launch {
            val result = runCatching {
                val json = backupRepository.export()
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(uri)?.use { out ->
                        out.write(json.toByteArray(Charsets.UTF_8))
                    } ?: error("Não foi possível abrir o arquivo de destino.")
                }
            }
            _backupMessage.value = result.fold(
                onSuccess = { "Backup exportado com sucesso." },
                onFailure = { "Falha ao exportar o backup." }
            )
        }
    }

    /** Restaura os dados a partir do arquivo escolhido (merge não-destrutivo). */
    fun importFrom(uri: Uri) {
        viewModelScope.launch {
            val result = runCatching {
                val json = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        input.readBytes().toString(Charsets.UTF_8)
                    } ?: error("Não foi possível abrir o arquivo selecionado.")
                }
                backupRepository.import(json)
            }
            _backupMessage.value = result.fold(
                onSuccess = { "Backup restaurado: ${it.tasks} tarefa(s), ${it.subtasks} subtarefa(s)." },
                onFailure = { it.message ?: "Falha ao restaurar o backup." }
            )
        }
    }

    fun saveTaskType(type: TaskType) {
        viewModelScope.launch { taskTypeRepository.saveType(type) }
    }

    fun deleteTaskType(type: TaskType, onUsed: () -> Unit) {
        viewModelScope.launch {
            val count = taskTypeRepository.getTaskCountForType(type.id)
            if (count > 0) {
                onUsed()
            } else {
                taskTypeRepository.deleteType(type)
            }
        }
    }
}
