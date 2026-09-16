package com.exoduss.cronos.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exoduss.cronos.ui.shared.TaskFormViewModel

/**
 * Host único dos bottom sheets de formulário e detalhe de tarefa.
 *
 * Antes, Home/Tarefas/Calendário/Avisos repetiam ~40 linhas idênticas cada
 * (coleta de flows + TaskFormSheet + TaskDetailSheet). Centralizado aqui.
 */
@Composable
fun TaskSheets(formViewModel: TaskFormViewModel) {
    val showFormSheet by formViewModel.showFormSheet.collectAsStateWithLifecycle()
    val formState by formViewModel.formState.collectAsStateWithLifecycle()
    val formSubtasks by formViewModel.formSubtasks.collectAsStateWithLifecycle()
    val isSaving by formViewModel.isSaving.collectAsStateWithLifecycle()
    val saveError by formViewModel.saveError.collectAsStateWithLifecycle()
    val allTypes by formViewModel.taskTypes.collectAsStateWithLifecycle()

    val showDetailSheet by formViewModel.showDetailSheet.collectAsStateWithLifecycle()
    val selectedTask by formViewModel.selectedTask.collectAsStateWithLifecycle()
    val selectedSubtasks by formViewModel.selectedTaskSubtasks.collectAsStateWithLifecycle()
    val selectedType by formViewModel.selectedTaskType.collectAsStateWithLifecycle()
    val selectedMedia by formViewModel.selectedTaskMedia.collectAsStateWithLifecycle()
    val pendingMediaTaskId by formViewModel.pendingMediaTaskId.collectAsStateWithLifecycle()

    TaskFormSheet(
        visible = showFormSheet,
        formState = formState,
        formSubtasks = formSubtasks,
        taskTypes = allTypes,
        onDismiss = { formViewModel.closeForm() },
        onUpdateTitle = formViewModel::updateTitle,
        onUpdateDescription = formViewModel::updateDescription,
        onUpdateTypeId = formViewModel::updateTypeId,
        onUpdatePriority = formViewModel::updatePriority,
        onUpdateDueDate = formViewModel::updateDueDate,
        onUpdateDueTime = formViewModel::updateDueTime,
        onUpdateRecurrence = formViewModel::updateRecurrence,
        onUpdatePinned = formViewModel::updatePinned,
        onUpdateReminder = formViewModel::updateReminder,
        onUpdateReminderFrequency = formViewModel::updateReminderFrequency,
        onUpdateReminderDaysBefore = formViewModel::updateReminderDaysBefore,
        onAddSubtask = formViewModel::addSubtask,
        onRemoveSubtask = formViewModel::removeSubtask,
        onToggleSubtask = formViewModel::toggleFormSubtask,
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
