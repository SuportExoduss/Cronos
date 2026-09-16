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
import com.exoduss.cronos.ui.components.TaskSheets
import com.exoduss.cronos.ui.components.TaskTimeline
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

    val today        = LocalDate.now()
    val hour         = LocalTime.now().hour
    val greeting     = when { hour < 12 -> "Bom dia"; hour < 18 -> "Boa tarde"; else -> "Boa noite" }
    val dateLabel    = today.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("pt", "BR")))
    val completedToday = todayTasks.count { it.status == TaskStatus.DONE }
    val totalToday     = todayTasks.size

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

            // ── Próximos compromissos (timeline) ──────────────────────────────
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.AccessTime, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)
                    )
                    Text(
                        "Próximos compromissos",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(10.dp))

                if (proximosTasks.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Você não tem compromissos próximos",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "Que tal organizar o próximo?",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = { formViewModel.openAddForm(preselectedDate = today) },
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Nova tarefa", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                } else {
                    TaskTimeline(
                        tasks = proximosTasks,
                        taskTypes = taskTypes,
                        onTap = { formViewModel.openDetail(it) }
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    TaskSheets(formViewModel)
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
