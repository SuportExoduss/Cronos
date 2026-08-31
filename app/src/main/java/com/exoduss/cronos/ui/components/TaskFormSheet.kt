package com.exoduss.cronos.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.exoduss.cronos.domain.model.Priority
import com.exoduss.cronos.domain.model.Recurrence
import com.exoduss.cronos.domain.model.TaskType
import com.exoduss.cronos.ui.shared.SubtaskFormItem
import com.exoduss.cronos.ui.shared.TaskFormState
import com.exoduss.cronos.ui.theme.PriorityHigh
import com.exoduss.cronos.ui.theme.PriorityLow
import com.exoduss.cronos.ui.theme.PriorityMedium
import com.exoduss.cronos.util.ReminderUtils
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskFormSheet(
    visible: Boolean,
    formState: TaskFormState,
    formSubtasks: List<SubtaskFormItem>,
    taskTypes: List<TaskType>,
    onDismiss: () -> Unit,
    onUpdateTitle: (String) -> Unit,
    onUpdateDescription: (String) -> Unit,
    onUpdateTypeId: (String?) -> Unit,
    onUpdatePriority: (Priority) -> Unit,
    onUpdateDueDate: (LocalDate?) -> Unit,
    onUpdateDueTime: (LocalTime?) -> Unit,
    onUpdateRecurrence: (Recurrence) -> Unit,
    onUpdatePinned: (Boolean) -> Unit,
    onUpdateReminder: (Boolean) -> Unit,
    onUpdateReminderFrequency: (Int) -> Unit,
    onUpdateReminderDaysBefore: (Int) -> Unit,
    onAddSubtask: (String) -> Unit,
    onRemoveSubtask: (String) -> Unit,
    onToggleSubtask: (String) -> Unit,
    onSave: () -> Unit,
    isSaving: Boolean = false,
    saveError: String? = null,
    onClearSaveError: () -> Unit = {}
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val titleFocusRequester = remember { FocusRequester() }
    var showMoreOptions by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var newSubtaskText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        try { titleFocusRequester.requestFocus() } catch (_: Exception) {}
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss, enabled = !isSaving) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar")
                }
                Text(
                    text = if (formState.editingId != null) "Editar Tarefa" else "Nova Tarefa",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = onSave,
                    enabled = formState.title.isNotBlank() && !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Salvar")
                    }
                }
            }

            // Faixa de erro — aparece quando o save falha
            if (saveError != null) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.ErrorOutline, null,
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        saveError,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onClearSaveError, modifier = Modifier.size(24.dp)) {
                        Icon(
                            Icons.Default.Close, null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Título
            OutlinedTextField(
                value = formState.title,
                onValueChange = { if (it.length <= TITLE_MAX) onUpdateTitle(it) },
                modifier = Modifier.fillMaxWidth().focusRequester(titleFocusRequester),
                placeholder = { Text("Título da tarefa *") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                isError = formState.title.isEmpty(),
                supportingText = {
                    Text(
                        "${formState.title.length}/$TITLE_MAX",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.End,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (formState.title.length >= TITLE_MAX)
                            MaterialTheme.colorScheme.error
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            Spacer(Modifier.height(16.dp))

            // Tipo
            if (taskTypes.isNotEmpty()) {
                Text("Tipo", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    taskTypes.forEach { type ->
                        val selected = formState.typeId == type.id
                        FilterChip(
                            selected = selected,
                            onClick = { onUpdateTypeId(if (selected) null else type.id) },
                            label = { Text(type.name, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = type.color().copy(alpha = 0.2f),
                                selectedLabelColor = type.color()
                            )
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // Prioridade
            Text("Prioridade", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    Priority.LOW to PriorityLow,
                    Priority.MEDIUM to PriorityMedium,
                    Priority.HIGH to PriorityHigh
                ).forEach { (p, color) ->
                    FilterChip(
                        selected = formState.priority == p,
                        onClick = { onUpdatePriority(p) },
                        label = { Text(p.label, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color.copy(alpha = 0.2f),
                            selectedLabelColor = color
                        )
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Data e hora
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CalendarMonth, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = formState.dueDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                            ?: "Sem data",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (formState.dueDate != null) {
                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Schedule, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = formState.dueTime?.format(DateTimeFormatter.ofPattern("HH:mm"))
                                ?: "Sem hora",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // Mais opções
            TextButton(
                onClick = { showMoreOptions = !showMoreOptions },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    if (showMoreOptions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null, Modifier.size(18.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Mais opções", style = MaterialTheme.typography.labelMedium)
            }

            AnimatedVisibility(visible = showMoreOptions) {
                Column {
                    // Descrição
                    OutlinedTextField(
                        value = formState.description,
                        onValueChange = { if (it.length <= DESC_MAX) onUpdateDescription(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Descrição (opcional)") },
                        minLines = 2,
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        supportingText = {
                            Text(
                                "${formState.description.length}/$DESC_MAX",
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.End,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (formState.description.length >= DESC_MAX)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )

                    Spacer(Modifier.height(16.dp))

                    // Recorrência
                    Text("Recorrência", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Recurrence.entries.forEach { rec ->
                            FilterChip(
                                selected = formState.recurrence == rec,
                                onClick = { onUpdateRecurrence(rec) },
                                label = { Text(rec.label, style = MaterialTheme.typography.labelSmall) }
                            )
                        }
                    }
                    if (formState.recurrence != Recurrence.NONE) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Ao concluir, a próxima ocorrência é criada automaticamente.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Lembrete toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Lembrete", style = MaterialTheme.typography.bodyMedium)
                            if (formState.dueDate == null) {
                                Text("Selecione uma data primeiro",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Switch(
                            checked = formState.reminderEnabled,
                            onCheckedChange = onUpdateReminder,
                            enabled = formState.dueDate != null
                        )
                    }

                    // Frequência de lembrete — aparece ao ativar o toggle
                    AnimatedVisibility(
                        visible = formState.reminderEnabled,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp, bottom = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text("Frequência de lembretes",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold)
                                    Text("${formState.reminderFrequency}x ao dia",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilledTonalIconButton(
                                        onClick = { onUpdateReminderFrequency(formState.reminderFrequency - 1) },
                                        enabled = formState.reminderFrequency > 1,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, null, Modifier.size(16.dp))
                                    }
                                    Text(
                                        "${formState.reminderFrequency}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.widthIn(min = 28.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    FilledTonalIconButton(
                                        onClick = { onUpdateReminderFrequency(formState.reminderFrequency + 1) },
                                        enabled = formState.reminderFrequency < 12,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Quantos dias antes começar
                            Text("Iniciar lembretes",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(6.dp))
                            val daysOptions = listOf(
                                0  to "No dia",
                                1  to "1 dia antes",
                                3  to "3 dias",
                                7  to "7 dias",
                                15 to "15 dias",
                                30 to "30 dias"
                            )
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                daysOptions.forEach { (days, label) ->
                                    val selected = formState.reminderDaysBefore == days
                                    FilterChip(
                                        selected = selected,
                                        onClick = { onUpdateReminderDaysBefore(days) },
                                        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }

                            Spacer(Modifier.height(12.dp))

                            // Preview dos horários gerados
                            val times = ReminderUtils.reminderHours(
                                formState.reminderFrequency,
                                formState.dueTime?.hour ?: 9
                            ).map { h -> "${h.toString().padStart(2, '0')}:00" }
                            Text("Horários de notificação:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(6.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                times.forEach { time ->
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Text(time,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Fixar no topo", style = MaterialTheme.typography.bodyMedium)
                        Switch(checked = formState.pinned, onCheckedChange = onUpdatePinned)
                    }

                    Spacer(Modifier.height(12.dp))

                    // Subtarefas
                    Text("Subtarefas", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))

                    formSubtasks.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = item.isDone,
                                onCheckedChange = { onToggleSubtask(item.id) }
                            )
                            Text(
                                item.title,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { onRemoveSubtask(item.id) }) {
                                Icon(Icons.Default.Close, null,
                                    tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newSubtaskText,
                            onValueChange = { newSubtaskText = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Adicionar subtarefa") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                onAddSubtask(newSubtaskText)
                                newSubtaskText = ""
                            })
                        )
                        IconButton(onClick = {
                            onAddSubtask(newSubtaskText)
                            newSubtaskText = ""
                        }) {
                            Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    // DatePickerDialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = formState.dueDate
                ?.atStartOfDay()
                ?.toEpochSecond(java.time.ZoneOffset.UTC)
                ?.times(1000)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneOffset.UTC)
                            .toLocalDate()
                        onUpdateDueDate(date)
                    }
                    showDatePicker = false
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    onUpdateDueDate(null)
                    onUpdateDueTime(null)
                    showDatePicker = false
                }) { Text("Limpar") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    // TimePickerDialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = formState.dueTime?.hour ?: 9,
            initialMinute = formState.dueTime?.minute ?: 0
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    onUpdateDueTime(LocalTime.of(timePickerState.hour, timePickerState.minute))
                    showTimePicker = false
                }) { Text("Confirmar") }
            },
            dismissButton = {
                TextButton(onClick = {
                    onUpdateDueTime(null)
                    showTimePicker = false
                }) { Text("Limpar") }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }
}

private const val TITLE_MAX = 100
private const val DESC_MAX  = 500

