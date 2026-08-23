package com.exoduss.cronos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.exoduss.cronos.domain.model.Task
import com.exoduss.cronos.domain.model.TaskStatus
import com.exoduss.cronos.domain.model.TaskType
import com.exoduss.cronos.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private data class KanbanColumn(
    val status: TaskStatus,
    val label: String,
    val color: Color
)

private val COLUMNS = listOf(
    KanbanColumn(TaskStatus.PENDING,     "Pendente",     PriorityMedium),
    KanbanColumn(TaskStatus.IN_PROGRESS, "Em progresso", TypeTrabalho),
    KanbanColumn(TaskStatus.DONE,        "Concluída",    SuccessLight),
    KanbanColumn(TaskStatus.CANCELLED,   "Cancelada",    MutedLight)
)

@Composable
fun KanbanBoard(
    tasks: List<Task>,
    taskTypes: List<TaskType>,
    onTapTask: (Task) -> Unit,
    onToggleComplete: (Task) -> Unit,
    onDeleteTask: (Task) -> Unit,
    onTogglePin: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(COLUMNS) { col ->
            val colTasks = tasks.filter { it.status == col.status }
            KanbanColumnView(
                column           = col,
                tasks            = colTasks,
                taskTypes        = taskTypes,
                onTap            = onTapTask,
                onToggleComplete = onToggleComplete,
                onDelete         = onDeleteTask,
                onTogglePin      = onTogglePin
            )
        }
    }
}

@Composable
private fun KanbanColumnView(
    column: KanbanColumn,
    tasks: List<Task>,
    taskTypes: List<TaskType>,
    onTap: (Task) -> Unit,
    onToggleComplete: (Task) -> Unit,
    onDelete: (Task) -> Unit,
    onTogglePin: (Task) -> Unit
) {
    Column(modifier = Modifier.width(265.dp).fillMaxHeight()) {
        // Cabeçalho
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                .background(column.color.copy(alpha = 0.12f))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(column.color)
                )
                Text(
                    column.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Surface(
                shape = RoundedCornerShape(50.dp),
                color = column.color.copy(alpha = 0.2f)
            ) {
                Text(
                    tasks.size.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = column.color,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        // Corpo
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp)),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ) {
            if (tasks.isEmpty()) {
                Box(
                    Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Text(
                        "Sem tarefas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(tasks, key = { it.id }) { task ->
                        val type = taskTypes.find { it.id == task.typeId }
                        KanbanCard(task, type, { onTap(task) }, { onToggleComplete(task) })
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        }
    }
}

@Composable
private fun KanbanCard(
    task: Task,
    taskType: TaskType?,
    onTap: () -> Unit,
    onToggleComplete: () -> Unit
) {
    val typeColor = taskType?.color() ?: TypePessoal
    val isDone    = task.status == TaskStatus.DONE || task.status == TaskStatus.CANCELLED
    val today     = LocalDate.now()

    Card(
        onClick   = onTap,
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Barra de cor no topo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(typeColor.copy(alpha = if (isDone) 0.3f else 1f))
            )
            Spacer(Modifier.height(9.dp))

            Text(
                task.title,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color      = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant
                             else MaterialTheme.colorScheme.onSurface,
                maxLines   = 2
            )

            if (task.description.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Text(
                    task.description,
                    style    = MaterialTheme.typography.bodySmall,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    taskType?.let {
                        Surface(
                            shape = RoundedCornerShape(50.dp),
                            color = typeColor.copy(alpha = 0.13f)
                        ) {
                            Text(
                                it.name,
                                style    = MaterialTheme.typography.labelSmall,
                                color    = typeColor,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Box(
                        Modifier.size(6.dp).clip(CircleShape)
                            .background(task.priority.priorityColor())
                    )
                }

                task.dueDate?.let { date ->
                    val isToday = date == today
                    Text(
                        if (isToday) "Hoje"
                        else date.format(DateTimeFormatter.ofPattern("dd/MM")),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isToday) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
