package com.exoduss.cronos.ui.tasks

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exoduss.cronos.domain.model.TaskStatus
import com.exoduss.cronos.ui.components.*
import com.exoduss.cronos.ui.profile.ProfileIcon
import com.exoduss.cronos.ui.shared.TaskFormViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    formViewModel: TaskFormViewModel,
    viewModel: TasksViewModel = hiltViewModel()
) {
    val tasks by viewModel.filteredTasks.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()
    val typeFilter by viewModel.typeFilter.collectAsStateWithLifecycle()
    val taskTypes by viewModel.taskTypes.collectAsStateWithLifecycle()
    val overdueCount by viewModel.overdueCount.collectAsStateWithLifecycle()

    val showFormSheet by formViewModel.showFormSheet.collectAsStateWithLifecycle()
    val formState by formViewModel.formState.collectAsStateWithLifecycle()
    val formSubtasks by formViewModel.formSubtasks.collectAsStateWithLifecycle()
    val isSaving by formViewModel.isSaving.collectAsStateWithLifecycle()
    val saveError by formViewModel.saveError.collectAsStateWithLifecycle()
    val showDetailSheet by formViewModel.showDetailSheet.collectAsStateWithLifecycle()
    val selectedTask by formViewModel.selectedTask.collectAsStateWithLifecycle()
    val selectedSubtasks by formViewModel.selectedTaskSubtasks.collectAsStateWithLifecycle()
    val selectedType by formViewModel.selectedTaskType.collectAsStateWithLifecycle()
    val selectedMedia by formViewModel.selectedTaskMedia.collectAsStateWithLifecycle()
    val pendingMediaTaskId by formViewModel.pendingMediaTaskId.collectAsStateWithLifecycle()
    val allTypes by formViewModel.taskTypes.collectAsStateWithLifecycle()

    // ── Vista: lista ou kanban ──────────────────────────────────────────────
    var isKanban by remember { mutableStateOf(false) }

    val statusOptions = listOf(
        null to "Todas",
        TaskStatus.PENDING to "Pendentes",
        TaskStatus.IN_PROGRESS to "Em progresso",
        TaskStatus.DONE to "Concluídas",
        TaskStatus.CANCELLED to "Canceladas"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tarefas", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                actions = {
                    // Toggle lista / kanban
                    IconButton(onClick = { isKanban = !isKanban }) {
                        Icon(
                            imageVector = if (isKanban) Icons.Default.ViewList else Icons.Default.ViewKanban,
                            contentDescription = if (isKanban) "Vista lista" else "Vista kanban",
                            tint = if (isKanban) MaterialTheme.colorScheme.primary
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    ProfileIcon()
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { formViewModel.openAddForm() },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = androidx.compose.foundation.shape.RoundedCornerShape(18.dp)
            ) { Icon(Icons.Default.Add, "Nova tarefa") }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {

            // Filtros — só na vista lista
            AnimatedVisibility(
                visible = !isKanban,
                enter = expandVertically() + fadeIn(),
                exit  = shrinkVertically() + fadeOut()
            ) {
                Column {
                    // Busca
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearch(it) },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("Buscar tarefas...") },
                        leadingIcon = { Icon(Icons.Default.Search, null) },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.setSearch("") }) {
                                    Icon(Icons.Default.Close, null)
                                }
                            }
                        },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )

                    // Filtro de status
                    LazyRow(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(statusOptions) { (status, label) ->
                            FilterChip(
                                selected = statusFilter == status,
                                onClick = { viewModel.setStatusFilter(if (statusFilter == status) null else status) },
                                label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // Filtro de tipo
                    if (taskTypes.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    selected = typeFilter == null,
                                    onClick = { viewModel.setTypeFilter(null) },
                                    label = { Text("Todos", style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                            items(taskTypes) { type ->
                                FilterChip(
                                    selected = typeFilter == type.id,
                                    onClick = { viewModel.setTypeFilter(if (typeFilter == type.id) null else type.id) },
                                    label = { Text(type.name, style = MaterialTheme.typography.labelSmall) }
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }

                    // Contador
                    Text(
                        text = "${tasks.size} tarefa(s)" + if (overdueCount > 0) " · $overdueCount atrasada(s)" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }

            // ── Vista Lista ────────────────────────────────────────────────
            AnimatedContent(
                targetState = isKanban,
                transitionSpec = {
                    (slideInHorizontally { if (targetState) it else -it } + fadeIn()) togetherWith
                    (slideOutHorizontally { if (targetState) -it else it } + fadeOut())
                },
                modifier = Modifier.fillMaxSize(),
                label = "view_toggle"
            ) { kanban ->
                if (kanban) {
                    // ── Vista Kanban ───────────────────────────────────────
                    KanbanBoard(
                        tasks            = tasks,
                        taskTypes        = taskTypes,
                        onTapTask        = { formViewModel.openDetail(it) },
                        onToggleComplete = { task ->
                            val newStatus = if (task.status == TaskStatus.DONE) TaskStatus.PENDING else TaskStatus.DONE
                            formViewModel.updateTaskStatus(task.id, newStatus)
                        },
                        onDeleteTask     = { formViewModel.deleteTask(it) },
                        onTogglePin      = { formViewModel.togglePin(it) },
                        modifier         = Modifier.fillMaxSize()
                    )
                } else {
                    // ── Vista Lista ────────────────────────────────────────
                    if (tasks.isEmpty()) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            EmptyState(
                                icon = Icons.Default.Assignment,
                                title = "Nenhuma tarefa encontrada",
                                subtitle = if (searchQuery.isNotBlank()) "Tente outro termo de busca"
                                           else "Toque no + para criar sua primeira tarefa"
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(tasks, key = { it.id }) { task ->
                                val type = taskTypes.find { it.id == task.typeId }
                                TaskCard(
                                    task     = task,
                                    taskType = type,
                                    onTap    = { formViewModel.openDetail(task) },
                                    onToggleComplete = {
                                        val newStatus = if (task.status == TaskStatus.DONE) TaskStatus.PENDING else TaskStatus.DONE
                                        formViewModel.updateTaskStatus(task.id, newStatus)
                                    },
                                    onDelete    = { formViewModel.deleteTask(task) },
                                    onTogglePin = { formViewModel.togglePin(task) }
                                )
                            }
                            item { Spacer(Modifier.height(80.dp)) }
                        }
                    }
                }
            }
        }
    }

    TaskFormSheet(
        visible = showFormSheet, formState = formState, formSubtasks = formSubtasks, taskTypes = allTypes,
        onDismiss = { formViewModel.closeForm() },
        onUpdateTitle = { formViewModel.updateTitle(it) },
        onUpdateDescription = { formViewModel.updateDescription(it) },
        onUpdateTypeId = { formViewModel.updateTypeId(it) },
        onUpdatePriority = { formViewModel.updatePriority(it) },
        onUpdateDueDate = { formViewModel.updateDueDate(it) },
        onUpdateDueTime = { formViewModel.updateDueTime(it) },
        onUpdateRecurrence = { formViewModel.updateRecurrence(it) },
        onUpdatePinned = { formViewModel.updatePinned(it) },
        onUpdateReminder = { formViewModel.updateReminder(it) },
        onUpdateReminderFrequency = { formViewModel.updateReminderFrequency(it) },
        onUpdateReminderDaysBefore = { formViewModel.updateReminderDaysBefore(it) },
        onAddSubtask = { formViewModel.addSubtask(it) },
        onRemoveSubtask = { formViewModel.removeSubtask(it) },
        onToggleSubtask = { formViewModel.toggleFormSubtask(it) },
        onSave = { formViewModel.saveTask() },
        isSaving = isSaving,
        saveError = saveError,
        onClearSaveError = { formViewModel.clearSaveError() }
    )

    TaskDetailSheet(
        visible = showDetailSheet, task = selectedTask, subtasks = selectedSubtasks,
        taskType = selectedType, media = selectedMedia,
        showMediaPrompt = selectedTask != null && pendingMediaTaskId == selectedTask?.id,
        onDismiss = { formViewModel.closeDetail() },
        onEdit = { formViewModel.editFromDetail() },
        onDelete = { selectedTask?.let { formViewModel.deleteTask(it) } },
        onUpdateStatus = { status -> selectedTask?.id?.let { formViewModel.updateTaskStatus(it, status) } },
        onToggleSubtask = { id, done -> formViewModel.toggleSubtaskInDetail(id, done) },
        onAddMedia = { uri, type -> selectedTask?.id?.let { formViewModel.addMedia(it, uri, type) } },
        onDeleteMedia = { formViewModel.deleteMedia(it) },
        onMediaPromptDismiss = { formViewModel.clearPendingMediaPrompt() }
    )
}
