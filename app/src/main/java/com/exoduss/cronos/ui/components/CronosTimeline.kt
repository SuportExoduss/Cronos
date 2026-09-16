package com.exoduss.cronos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.exoduss.cronos.domain.model.Priority
import com.exoduss.cronos.domain.model.Task
import com.exoduss.cronos.domain.model.TaskType
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dayFmt = DateTimeFormatter.ofPattern("dd/MM")

/**
 * Linha do tempo vertical dos próximos compromissos.
 *
 * Substitui cards empilhados por uma timeline contínua (conceito TEMPO → FLUXO),
 * usando as cores atuais do tema. Cada item abre o detalhe ao toque.
 */
@Composable
fun TaskTimeline(
    tasks: List<Task>,
    taskTypes: List<TaskType>,
    onTap: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        tasks.forEachIndexed { index, task ->
            TimelineRow(
                task = task,
                type = taskTypes.find { it.id == task.typeId },
                isFirst = index == 0,
                isLast = index == tasks.lastIndex,
                onTap = { onTap(task) }
            )
        }
    }
}

@Composable
private fun TimelineRow(
    task: Task,
    type: TaskType?,
    isFirst: Boolean,
    isLast: Boolean,
    onTap: () -> Unit
) {
    val dotColor = type?.color() ?: MaterialTheme.colorScheme.primary
    val lineColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    val today = LocalDate.now()

    val timeLabel = task.dueTime?.let { "%02d:%02d".format(it.hour, it.minute) }
    val dateLabel = when (task.dueDate) {
        today -> "Hoje"
        today.plusDays(1) -> "Amanhã"
        else -> task.dueDate?.format(dayFmt) ?: ""
    }
    val markerTop = timeLabel ?: dateLabel
    val markerBottom = if (timeLabel != null && task.dueDate != today) dateLabel else null

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onTap)
            .height(IntrinsicSize.Min)
    ) {
        // Marcador temporal
        Column(
            modifier = Modifier.width(50.dp).padding(top = 1.dp),
            horizontalAlignment = Alignment.End
        ) {
            Text(
                markerTop,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            if (markerBottom != null) {
                Text(
                    markerBottom,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }

        // Conector: linha contínua + ponto da categoria
        Column(
            modifier = Modifier.width(24.dp).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.width(2.dp).height(6.dp)
                    .background(if (isFirst) Color.Transparent else lineColor)
            )
            Box(Modifier.size(10.dp).clip(CircleShape).background(dotColor))
            Box(
                Modifier.width(2.dp).weight(1f)
                    .background(if (isLast) Color.Transparent else lineColor)
            )
        }

        // Conteúdo
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 6.dp, bottom = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (task.priority == Priority.HIGH) {
                    Box(Modifier.size(6.dp).clip(CircleShape).background(task.priority.priorityColor()))
                }
                Text(
                    task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            val subtitle = task.description.ifBlank { type?.name ?: "" }
            if (subtitle.isNotBlank()) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
