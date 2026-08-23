package com.exoduss.cronos.ui.notifications

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exoduss.cronos.domain.model.Task
import com.exoduss.cronos.domain.model.TaskStatus
import com.exoduss.cronos.domain.model.TaskType
import com.exoduss.cronos.domain.model.isOverdue
import com.exoduss.cronos.ui.components.EmptyState
import com.exoduss.cronos.ui.components.TaskDetailSheet
import com.exoduss.cronos.ui.components.TaskFormSheet
import com.exoduss.cronos.ui.components.color
import com.exoduss.cronos.ui.shared.TaskFormViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    formViewModel: TaskFormViewModel,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val overdueTasks by viewModel.overdueTasks.collectAsStateWithLifecycle()
    val todayTasks by viewModel.todayTasks.collectAsStateWithLifecycle()
    val upcomingTasks by viewModel.upcomingTasks.collectAsStateWithLifecycle()
    val taskTypes by viewModel.taskTypes.collectAsStateWithLifecycle()

    val showDetailSheet by formViewModel.showDetailSheet.collectAsStateWithLifecycle()
    val selectedTask by formViewModel.selectedTask.collectAsStateWithLifecycle()
    val selectedSubtasks by formViewModel.selectedTaskSubtasks.collectAsStateWithLifecycle()
    val selectedType by formViewModel.selectedTaskType.collectAsStateWithLifecycle()
    val selectedMedia by formViewModel.selectedTaskMedia.collectAsStateWithLifecycle()
    val pendingMediaTaskId by formViewModel.pendingMediaTaskId.collectAsStateWithLifecycle()

    val showFormSheet by formViewModel.showFormSheet.collectAsStateWithLifecycle()
    val formState by formViewModel.formState.collectAsStateWithLifecycle()
    val formSubtasks by formViewModel.formSubtasks.collectAsStateWithLifecycle()
    val isSaving by formViewModel.isSaving.collectAsStateWithLifecycle()
    val saveError by formViewModel.saveError.collectAsStateWithLifecycle()
    val allTypes by formViewModel.taskTypes.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Avisos", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        if (overdueTasks.isEmpty() && todayTasks.isEmpty() && upcomingTasks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState(
                    icon = Icons.Default.NotificationsNone,
                    title = "Nenhuma tarefa pendente",
                    subtitle = "Você está em dia com tudo!"
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (overdueTasks.isNotEmpty()) {
                    item {
                        SectionHeader(Icons.Default.Warning, "Atrasadas (${overdueTasks.size})",
                            MaterialTheme.colorScheme.error)
                    }
                    items(overdueTasks, key = { "o_${it.id}" }) { task ->
                        val type = taskTypes.find { it.id == task.typeId }
                        NotifCard(task, type, isOverdue = true, onTap = { formViewModel.openDetail(task) })
                    }
                }

                if (todayTasks.isNotEmpty()) {
                    item {
                        SectionHeader(Icons.Default.Today, "Hoje (${todayTasks.size})",
                            MaterialTheme.colorScheme.primary)
                    }
                    items(todayTasks, key = { "t_${it.id}" }) { task ->
                        val type = taskTypes.find { it.id == task.typeId }
                        NotifCard(task, type, isOverdue = false, onTap = { formViewModel.openDetail(task) })
                    }
                }

                if (upcomingTasks.isNotEmpty()) {
                    item {
                        SectionHeader(Icons.Default.Schedule, "Próximas (${upcomingTasks.size})",
                            MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    items(upcomingTasks, key = { "u_${it.id}" }) { task ->
                        val type = taskTypes.find { it.id == task.typeId }
                        NotifCard(task, type, isOverdue = false, onTap = { formViewModel.openDetail(task) })
                    }
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    TaskFormSheet(
        visible = showFormSheet, formState = formState, formSubtasks = formSubtasks, taskTypes = allTypes,
        onDismiss = { formViewModel.closeForm() },
        onUpdateTitle = { formViewModel.updateTitle(it) }, onUpdateDescription = { formViewModel.updateDescription(it) },
        onUpdateTypeId = { formViewModel.updateTypeId(it) }, onUpdatePriority = { formViewModel.updatePriority(it) },
        onUpdateDueDate = { formViewModel.updateDueDate(it) }, onUpdateDueTime = { formViewModel.updateDueTime(it) },
        onUpdateRecurrence = { formViewModel.updateRecurrence(it) }, onUpdatePinned = { formViewModel.updatePinned(it) },
        onUpdateReminder = { formViewModel.updateReminder(it) },
        onUpdateReminderFrequency = { formViewModel.updateReminderFrequency(it) },
        onUpdateReminderDaysBefore = { formViewModel.updateReminderDaysBefore(it) },
        onAddSubtask = { formViewModel.addSubtask(it) },
        onRemoveSubtask = { formViewModel.removeSubtask(it) }, onToggleSubtask = { formViewModel.toggleFormSubtask(it) },
        onSave = { formViewModel.saveTask() },
        isSaving = isSaving,
        saveError = saveError,
        onClearSaveError = { formViewModel.clearSaveError() }
    )

    TaskDetailSheet(
        visible = showDetailSheet,
        task = selectedTask,
        subtasks = selectedSubtasks,
        taskType = selectedType,
        media = selectedMedia,
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

@Composable
private fun SectionHeader(icon: ImageVector, title: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = color)
    }
}

@Composable
private fun NotifCard(task: Task, taskType: TaskType?, isOverdue: Boolean, onTap: () -> Unit) {
    val typeColor = taskType?.color() ?: MaterialTheme.colorScheme.onSurfaceVariant
    Card(
        onClick = onTap,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOverdue) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                if (isOverdue) Icons.Default.ErrorOutline else Icons.Default.Notifications,
                null,
                tint = if (isOverdue) MaterialTheme.colorScheme.error else typeColor,
                modifier = Modifier.size(24.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(task.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                task.dueDate?.let { date ->
                    Text(
                        text = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOverdue) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            taskType?.let { type ->
                Surface(shape = RoundedCornerShape(20.dp), color = typeColor.copy(alpha = 0.15f)) {
                    Text(
                        type.name, style = MaterialTheme.typography.labelSmall,
                        color = typeColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
