package com.exoduss.cronos.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exoduss.cronos.domain.model.Task
import com.exoduss.cronos.domain.model.TaskStatus
import com.exoduss.cronos.domain.model.TaskType
import com.exoduss.cronos.ui.components.TaskCard
import com.exoduss.cronos.ui.components.TaskDetailSheet
import com.exoduss.cronos.ui.components.TaskFormSheet
import com.exoduss.cronos.ui.profile.ProfileIcon
import com.exoduss.cronos.ui.shared.TaskFormViewModel
import com.exoduss.cronos.ui.theme.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    formViewModel: TaskFormViewModel,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val userName        by viewModel.userName.collectAsStateWithLifecycle()
    val todayTasks      by viewModel.todayTasks.collectAsStateWithLifecycle()
    val pendingTasks    by viewModel.pendingTasks.collectAsStateWithLifecycle()
    val overdueTasks    by viewModel.overdueTasks.collectAsStateWithLifecycle()
    val taskTypes       by viewModel.taskTypes.collectAsStateWithLifecycle()

    val showFormSheet   by formViewModel.showFormSheet.collectAsStateWithLifecycle()
    val formState       by formViewModel.formState.collectAsStateWithLifecycle()
    val formSubtasks    by formViewModel.formSubtasks.collectAsStateWithLifecycle()
    val isSaving        by formViewModel.isSaving.collectAsStateWithLifecycle()
    val saveError       by formViewModel.saveError.collectAsStateWithLifecycle()
    val showDetailSheet by formViewModel.showDetailSheet.collectAsStateWithLifecycle()
    val selectedTask    by formViewModel.selectedTask.collectAsStateWithLifecycle()
    val selectedSubs    by formViewModel.selectedTaskSubtasks.collectAsStateWithLifecycle()
    val selectedType    by formViewModel.selectedTaskType.collectAsStateWithLifecycle()
    val selectedMedia   by formViewModel.selectedTaskMedia.collectAsStateWithLifecycle()
    val pendingMediaId  by formViewModel.pendingMediaTaskId.collectAsStateWithLifecycle()
    val allTypes        by formViewModel.taskTypes.collectAsStateWithLifecycle()

    val today        = LocalDate.now()
    val hour         = LocalTime.now().hour
    val greeting     = when { hour < 12 -> "Bom dia"; hour < 18 -> "Boa tarde"; else -> "Boa noite" }
    val dateLabel    = today.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("pt", "BR")))
    val completedToday = todayTasks.count { it.status == TaskStatus.DONE }
    val totalToday     = todayTasks.size
    val inProgressCount = todayTasks.count { it.status == TaskStatus.IN_PROGRESS }
    val scheduledCount  = pendingTasks.size

    val proximosTasks = (todayTasks.filter { it.status != TaskStatus.DONE && it.status != TaskStatus.CANCELLED } +
            pendingTasks).distinctBy { it.id }.take(5)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(modifier = Modifier.width(18.dp).height(1.dp).background(GoldDark))
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "CRONOS",
                            color = GoldPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 3.sp
                        )
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = GoldPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Box(modifier = Modifier.width(18.dp).height(1.dp).background(GoldDark))
                    }
                },
                actions = {
                    ProfileIcon()
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { formViewModel.openAddForm(preselectedDate = today) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor   = Color.White,
                shape          = CircleShape
            ) { Icon(Icons.Default.Add, "Nova tarefa") }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Saudação ────────────────────────────────────────────────────
            item {
                Spacer(Modifier.height(4.dp))
                Column {
                    Row {
                        Text(
                            text = if (userName.isNotBlank()) "$greeting, " else "$greeting!",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (userName.isNotBlank()) {
                            Text(
                                text = "$userName!",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Text(
                        text = dateLabel.replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // ── Resumo de Hoje ───────────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "RESUMO DE HOJE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$completedToday/$totalToday",
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "tarefas concluídas",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            LinearProgressIndicator(
                                progress = { if (totalToday > 0) completedToday.toFloat() / totalToday else 0f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.outline
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Continue assim!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Icon(
                                    Icons.Default.FlashOn,
                                    contentDescription = null,
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        CompletionDial(
                            progress = if (totalToday > 0) completedToday.toFloat() / totalToday else 0f,
                            modifier = Modifier.size(90.dp)
                        )
                    }
                }
            }

            // ── Grid de estatísticas ─────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatBox(
                        icon     = Icons.Default.RadioButtonUnchecked,
                        iconTint = MaterialTheme.colorScheme.primary,
                        count    = overdueTasks.size + todayTasks.count { it.status == TaskStatus.PENDING },
                        label    = "Pendentes",
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        icon     = Icons.Default.Timelapse,
                        iconTint = WarningDark,
                        count    = inProgressCount,
                        label    = "Em progresso",
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        icon     = Icons.Default.CheckCircle,
                        iconTint = BluePrimary,
                        count    = completedToday,
                        label    = "Concluídas",
                        modifier = Modifier.weight(1f)
                    )
                    StatBox(
                        icon     = Icons.Default.CalendarMonth,
                        iconTint = TypeEstudo,
                        count    = scheduledCount,
                        label    = "Agendadas",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── Atrasadas ─────────────────────────────────────────────────────
            if (overdueTasks.isNotEmpty()) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error)
                        )
                        Text(
                            "Atrasadas (${overdueTasks.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                items(overdueTasks, key = { "overdue_${it.id}" }) { task ->
                    TaskCardItem(task, taskTypes, formViewModel)
                }
            }

            // ── Próximos compromissos ─────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AccessTime,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Text(
                                "Próximos compromissos",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (proximosTasks.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp, vertical = 28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Você não tem compromissos próximos",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    "Crie tarefas ou eventos para vê-los aqui.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { formViewModel.openAddForm(preselectedDate = today) },
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Criar nova tarefa", style = MaterialTheme.typography.labelLarge)
                                }
                            }
                        }
                    }
                }
            }

            // Tarefas dentro de "Próximos compromissos"
            if (proximosTasks.isNotEmpty()) {
                items(proximosTasks, key = { "prox_${it.id}" }) { task ->
                    TaskCardItem(task, taskTypes, formViewModel)
                }
            }

            // ── Foco do dia ───────────────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.GpsFixed,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Foco do dia",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                "Defina sua prioridade principal para hoje",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        OutlinedButton(
                            onClick = {},
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text("Definir foco", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            // ── Banner premium ────────────────────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldSurface),
                    border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(GoldPrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = GoldPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Desbloqueie todo o potencial do Cronos",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = GoldPrimary
                            )
                            Text(
                                "Faça upgrade e tenha mais recursos exclusivos.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = {},
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GoldPrimary,
                                contentColor   = Color(0xFF0B0D12)
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                "Ver planos",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    TaskFormSheet(
        visible              = showFormSheet,
        formState            = formState,
        formSubtasks         = formSubtasks,
        taskTypes            = allTypes,
        onDismiss            = { formViewModel.closeForm() },
        onUpdateTitle        = { formViewModel.updateTitle(it) },
        onUpdateDescription  = { formViewModel.updateDescription(it) },
        onUpdateTypeId       = { formViewModel.updateTypeId(it) },
        onUpdatePriority     = { formViewModel.updatePriority(it) },
        onUpdateDueDate      = { formViewModel.updateDueDate(it) },
        onUpdateDueTime      = { formViewModel.updateDueTime(it) },
        onUpdateRecurrence   = { formViewModel.updateRecurrence(it) },
        onUpdatePinned       = { formViewModel.updatePinned(it) },
        onUpdateReminder     = { formViewModel.updateReminder(it) },
        onUpdateReminderFrequency   = { formViewModel.updateReminderFrequency(it) },
        onUpdateReminderDaysBefore  = { formViewModel.updateReminderDaysBefore(it) },
        onAddSubtask         = { formViewModel.addSubtask(it) },
        onRemoveSubtask      = { formViewModel.removeSubtask(it) },
        onToggleSubtask      = { formViewModel.toggleFormSubtask(it) },
        onSave               = { formViewModel.saveTask() },
        isSaving             = isSaving,
        saveError            = saveError,
        onClearSaveError     = { formViewModel.clearSaveError() }
    )

    TaskDetailSheet(
        visible          = showDetailSheet,
        task             = selectedTask,
        subtasks         = selectedSubs,
        taskType         = selectedType,
        media            = selectedMedia,
        showMediaPrompt  = selectedTask != null && pendingMediaId == selectedTask?.id,
        onDismiss        = { formViewModel.closeDetail() },
        onEdit           = { formViewModel.editFromDetail() },
        onDelete         = { selectedTask?.let { formViewModel.deleteTask(it) } },
        onUpdateStatus   = { status -> selectedTask?.id?.let { formViewModel.updateTaskStatus(it, status) } },
        onToggleSubtask  = { id, done -> formViewModel.toggleSubtaskInDetail(id, done) },
        onAddMedia       = { uri, type -> selectedTask?.id?.let { formViewModel.addMedia(it, uri, type) } },
        onDeleteMedia    = { formViewModel.deleteMedia(it) },
        onMediaPromptDismiss = { formViewModel.clearPendingMediaPrompt() }
    )
}

// ── Dial de conclusão ─────────────────────────────────────────────────────────
@Composable
private fun CompletionDial(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx     = size.width / 2f
            val cy     = size.height / 2f
            val outerR = size.minDimension / 2f * 0.96f
            val count  = 60
            for (i in 0 until count) {
                val angle   = (i.toFloat() / count * 360f - 90f) * PI.toFloat() / 180f
                val isLong  = i % 5 == 0
                val innerR  = outerR * (if (isLong) 0.70f else 0.78f)
                val filled  = progress > 0f && i.toFloat() / count < progress
                drawLine(
                    color       = if (filled) primaryColor else primaryColor.copy(alpha = 0.20f),
                    start       = Offset(cx + innerR * cos(angle), cy + innerR * sin(angle)),
                    end         = Offset(cx + outerR * cos(angle), cy + outerR * sin(angle)),
                    strokeWidth = if (isLong) 3.dp.toPx() else 1.5.dp.toPx(),
                    cap         = StrokeCap.Round
                )
            }
        }
        Icon(
            imageVector        = Icons.Default.CheckCircle,
            contentDescription = null,
            tint               = primaryColor,
            modifier           = Modifier.size(36.dp)
        )
    }
}

// ── Caixa de estatística ──────────────────────────────────────────────────────
@Composable
private fun StatBox(
    icon:     ImageVector,
    iconTint: Color,
    count:    Int,
    label:    String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(17.dp))
            }
            Text(
                text       = count.toString(),
                style      = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color      = iconTint
            )
            Text(
                text   = label,
                style  = MaterialTheme.typography.labelSmall,
                color  = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

// ── TaskCard helper ───────────────────────────────────────────────────────────
@Composable
private fun TaskCardItem(
    task: Task,
    taskTypes: List<TaskType>,
    formViewModel: TaskFormViewModel
) {
    val type = taskTypes.find { it.id == task.typeId }
    TaskCard(
        task           = task,
        taskType       = type,
        onTap          = { formViewModel.openDetail(task) },
        onToggleComplete = {
            val next = if (task.status == TaskStatus.DONE) TaskStatus.PENDING else TaskStatus.DONE
            formViewModel.updateTaskStatus(task.id, next)
        },
        onDelete       = { formViewModel.deleteTask(task) },
        onTogglePin    = { formViewModel.togglePin(task) }
    )
}
