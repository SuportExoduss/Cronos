package com.exoduss.cronos.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.exoduss.cronos.domain.model.Priority
import com.exoduss.cronos.domain.model.Task
import com.exoduss.cronos.domain.model.TaskStatus
import com.exoduss.cronos.domain.model.TaskType
import com.exoduss.cronos.domain.model.isOverdue
import com.exoduss.cronos.ui.theme.*
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCard(
    task: Task,
    taskType: TaskType?,
    onTap: () -> Unit,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var justCompleted by remember { mutableStateOf(false) }

    // ── Animação de conclusão ───────────────────────────────────────────────
    val checkScale = remember { Animatable(1f) }
    val cardFlash  = remember { Animatable(0f) }

    LaunchedEffect(task.status) {
        if (task.status == TaskStatus.DONE && justCompleted) {
            // Bounce no checkbox
            checkScale.animateTo(1.45f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh))
            checkScale.animateTo(1f,    spring(dampingRatio = Spring.DampingRatioMediumBouncy))
            // Flash verde no card
            cardFlash.animateTo(1f, tween(120))
            delay(180)
            cardFlash.animateTo(0f, tween(350))
            justCompleted = false
        }
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd) {
                showDeleteDialog = true; false
            } else false
        }
    )

    val isDone    = task.status == TaskStatus.DONE || task.status == TaskStatus.CANCELLED
    val isOverdue = task.isOverdue()
    val typeColor = taskType?.color() ?: TypePessoal
    val today     = LocalDate.now()

    // Flash verde sobreposto ao card
    val flashColor by animateColorAsState(
        targetValue = if (cardFlash.value > 0f) SuccessLight.copy(alpha = cardFlash.value * 0.18f)
                      else Color.Transparent,
        label = "card_flash"
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromEndToStart = false,
        backgroundContent = {
            val bg by animateColorAsState(
                if (dismissState.dismissDirection == SwipeToDismissBoxValue.StartToEnd)
                    ErrorLight.copy(alpha = 0.12f)
                else Color.Transparent,
                label = "swipe_bg"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(bg)
                    .padding(start = 20.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(Icons.Default.Delete, null, tint = ErrorLight, modifier = Modifier.size(20.dp))
            }
        },
        modifier = modifier
    ) {
        Card(
            onClick = onTap,
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (isDone) 0.55f else 1f)
                .pointerInput(Unit) { detectTapGestures(onLongPress = { onTogglePin() }) },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isOverdue -> MaterialTheme.colorScheme.error.copy(alpha = 0.07f)
                    else      -> MaterialTheme.colorScheme.surface
                }
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (task.pinned && !isDone) 3.dp else 0.dp
            ),
            border = when {
                isOverdue -> androidx.compose.foundation.BorderStroke(
                    1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.35f))
                task.pinned && !isDone -> androidx.compose.foundation.BorderStroke(
                    1.dp, typeColor.copy(alpha = 0.5f))
                else -> null
            }
        ) {
            Box {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Accent bar com gradiente suave
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .height(48.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(typeColor, typeColor.copy(alpha = 0.4f))
                                )
                            )
                    )

                    Spacer(Modifier.width(14.dp))

                    // Checkbox com animação de escala
                    Box(modifier = Modifier.scale(checkScale.value)) {
                        Checkbox(
                            checked = isDone,
                            onCheckedChange = {
                                justCompleted = !isDone  // só anima ao completar, não ao descompletar
                                onToggleComplete()
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor   = MaterialTheme.colorScheme.primary,
                                uncheckedColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.7f)
                            )
                        )
                    }

                    Spacer(Modifier.width(6.dp))

                    // Conteúdo
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (task.pinned && !isDone) {
                                Icon(
                                    Icons.Default.PushPin, null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(11.dp)
                                )
                            }
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (!isDone) FontWeight.Medium else FontWeight.Normal,
                                textDecoration = if (isDone) TextDecoration.LineThrough else null,
                                color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant
                                        else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (task.description.isNotBlank()) {
                            Text(
                                text = task.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(Modifier.height(5.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Badge tipo — pílula mais suave
                            taskType?.let { type ->
                                Surface(
                                    shape = RoundedCornerShape(50.dp),
                                    color = type.color().copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        type.name,
                                        style  = MaterialTheme.typography.labelSmall,
                                        color  = type.color(),
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            // Dot prioridade
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(task.priority.priorityColor())
                            )

                            // Data
                            task.dueDate?.let { date ->
                                val isToday   = date == today
                                val dateColor = when {
                                    isOverdue -> ErrorLight
                                    isToday   -> MaterialTheme.colorScheme.primary
                                    else      -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                                val dateStr = when {
                                    isToday      -> "Hoje"
                                    date == today.plusDays(1) -> "Amanhã"
                                    else         -> date.format(DateTimeFormatter.ofPattern("dd/MM"))
                                }
                                Surface(
                                    shape = RoundedCornerShape(50.dp),
                                    color = dateColor.copy(alpha = if (isToday || isOverdue) 0.12f else 0f)
                                ) {
                                    Text(
                                        dateStr,
                                        style  = MaterialTheme.typography.labelSmall,
                                        color  = dateColor,
                                        fontWeight = if (isToday || isOverdue) FontWeight.SemiBold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = if (isToday || isOverdue) 7.dp else 0.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Flash de conclusão sobreposto
                if (flashColor != Color.Transparent) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(20.dp))
                            .background(flashColor)
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text("Excluir tarefa?", fontWeight = FontWeight.SemiBold) },
            text  = { Text("Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = { showDeleteDialog = false; onDelete() },
                    colors  = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Excluir", fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

fun TaskType.color(): Color = try {
    Color(android.graphics.Color.parseColor(this.color))
} catch (e: Exception) {
    TypePessoal
}

fun Priority.priorityColor(): Color = when (this) {
    Priority.LOW    -> PriorityLow
    Priority.MEDIUM -> PriorityMedium
    Priority.HIGH   -> PriorityHigh
}
