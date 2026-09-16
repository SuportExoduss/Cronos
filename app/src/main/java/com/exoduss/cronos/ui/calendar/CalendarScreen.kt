package com.exoduss.cronos.ui.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exoduss.cronos.domain.model.Task
import com.exoduss.cronos.domain.model.TaskStatus
import com.exoduss.cronos.domain.model.isOverdue
import com.exoduss.cronos.ui.components.EmptyState
import com.exoduss.cronos.ui.components.TaskCard
import com.exoduss.cronos.ui.components.TaskSheets
import com.exoduss.cronos.ui.profile.ProfileIcon
import com.exoduss.cronos.ui.shared.TaskFormViewModel
import com.exoduss.cronos.ui.theme.*
import com.exoduss.cronos.util.BrazilHolidays
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private val HolidayBg   = Color(0xFFFFF3CD)
private val HolidayText = Color(0xFF856404)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    formViewModel: TaskFormViewModel,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val selectedDate    by viewModel.selectedDate.collectAsStateWithLifecycle()
    val currentMonth    by viewModel.currentMonth.collectAsStateWithLifecycle()
    val allTasks        by viewModel.allTasks.collectAsStateWithLifecycle()
    val filteredTasks   by viewModel.filteredTasks.collectAsStateWithLifecycle()
    val calendarFilter  by viewModel.calendarFilter.collectAsStateWithLifecycle()
    val taskTypes       by viewModel.taskTypes.collectAsStateWithLifecycle()

    val monthLabel = currentMonth
        .format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale("pt", "BR")))
        .replaceFirstChar { it.uppercase() }

    val holidaysForMonth = remember(currentMonth) { BrazilHolidays.getHolidaysForMonth(currentMonth) }
    val holidaysForDay   = remember(selectedDate) { BrazilHolidays.getHolidaysForDate(selectedDate) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendário", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                actions = { ProfileIcon() }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { formViewModel.openAddForm(preselectedDate = selectedDate) },
                containerColor = MaterialTheme.colorScheme.primary
            ) { Icon(Icons.Default.Add, "Nova tarefa") }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Month navigation
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.previousMonth() }) {
                        Icon(Icons.Default.ChevronLeft, "Mês anterior")
                    }
                    Text(monthLabel,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    IconButton(onClick = { viewModel.nextMonth() }) {
                        Icon(Icons.Default.ChevronRight, "Próximo mês")
                    }
                }
            }

            // Day-of-week headers
            item {
                Row(modifier = Modifier.fillMaxWidth()) {
                    listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Calendar grid
            item {
                CalendarGrid(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    allTasks = allTasks,
                    holidayDates = holidaysForMonth.map { it.first }.toSet(),
                    onDateSelected = { viewModel.selectDate(it) }
                )
            }

            // Filter chips
            item {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CalendarFilter.entries.forEach { f ->
                        FilterChip(
                            selected = calendarFilter == f,
                            onClick = { viewModel.setFilter(f) },
                            label = {
                                Text(
                                    when (f) {
                                        CalendarFilter.MONTH -> "Mês"
                                        CalendarFilter.WEEK  -> "Semana"
                                        CalendarFilter.DAY   -> "Dia"
                                    },
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        )
                    }
                }
            }

            // Month holidays (collapsible)
            if (holidaysForMonth.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(12.dp))
                    MonthHolidaysCard(holidays = holidaysForMonth)
                }
            }

            // Day section divider + label
            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                val dateLabel = selectedDate.format(
                    DateTimeFormatter.ofPattern("d 'de' MMMM", Locale("pt", "BR"))
                )
                Text(
                    text = dateLabel.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Day holidays (expandable)
            if (holidaysForDay.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    DayHolidaysCard(holidays = holidaysForDay, selectedDate = selectedDate)
                }
            }

            // Create task button
            item {
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { formViewModel.openAddForm(preselectedDate = selectedDate) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Criar tarefa neste dia")
                }
                Spacer(Modifier.height(8.dp))
            }

            // Task list for selected period
            if (filteredTasks.isEmpty()) {
                item {
                    EmptyState(
                        icon = Icons.Default.EventAvailable,
                        title = "Sem tarefas nesse período",
                        subtitle = "Toque em 'Criar tarefa' para adicionar"
                    )
                }
            } else {
                items(filteredTasks, key = { it.id }) { task ->
                    val type = taskTypes.find { it.id == task.typeId }
                    TaskCard(
                        task = task,
                        taskType = type,
                        onTap = { formViewModel.openDetail(task) },
                        onToggleComplete = {
                            val newStatus = if (task.status == TaskStatus.DONE) TaskStatus.PENDING else TaskStatus.DONE
                            formViewModel.updateTaskStatus(task.id, newStatus)
                        },
                        onDelete = { formViewModel.deleteTask(task) },
                        onTogglePin = { formViewModel.togglePin(task) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    TaskSheets(formViewModel)
}

@Composable
private fun MonthHolidaysCard(holidays: List<Pair<LocalDate, String>>) {
    var expanded by remember { mutableStateOf(false) }
    val dayFmt = DateTimeFormatter.ofPattern("d/MM", Locale("pt", "BR"))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = HolidayBg)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Celebration, null,
                        tint = HolidayText, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Feriados do mês (${holidays.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = HolidayText, fontWeight = FontWeight.SemiBold)
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null, tint = HolidayText, modifier = Modifier.size(20.dp)
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    holidays.forEach { (date, name) ->
                        Text(
                            "• ${date.format(dayFmt)} — $name",
                            style = MaterialTheme.typography.bodySmall,
                            color = HolidayText,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayHolidaysCard(holidays: List<String>, selectedDate: LocalDate) {
    var expanded by remember(selectedDate) { mutableStateOf(true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = HolidayBg)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Celebration, null,
                        tint = HolidayText, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Feriado",
                        style = MaterialTheme.typography.labelMedium,
                        color = HolidayText, fontWeight = FontWeight.SemiBold)
                }
                Icon(
                    if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    null, tint = HolidayText, modifier = Modifier.size(16.dp)
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 4.dp)) {
                    holidays.forEach { name ->
                        Text("• $name",
                            style = MaterialTheme.typography.bodySmall,
                            color = HolidayText,
                            modifier = Modifier.padding(vertical = 1.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: YearMonth,
    selectedDate: LocalDate,
    allTasks: List<Task>,
    holidayDates: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val firstDay = currentMonth.atDay(1)
    val startOffset = firstDay.dayOfWeek.value % 7
    val daysInMonth = currentMonth.lengthOfMonth()
    val tasksByDate = allTasks.groupBy { it.dueDate }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        var dayIndex = 1
        repeat(6) { week ->
            if (dayIndex > daysInMonth) return@repeat
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { dow ->
                    val cellDay = week * 7 + dow + 1 - startOffset
                    if (cellDay < 1 || cellDay > daysInMonth) {
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    } else {
                        val date = currentMonth.atDay(cellDay)
                        val isSelected = date == selectedDate
                        val isToday = date == today
                        val isHoliday = date in holidayDates
                        val tasksForDay = tasksByDate[date] ?: emptyList()
                        val hasTasks = tasksForDay.isNotEmpty()
                        val hasOverdue = tasksForDay.any { it.isOverdue() }
                        val allDone = hasTasks && tasksForDay.all { it.status == TaskStatus.DONE }
                        val hasPending = tasksForDay.any {
                            it.status != TaskStatus.DONE && it.status != TaskStatus.CANCELLED
                        }

                        val dotColor = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            hasOverdue -> DotAtrasada
                            isToday && hasPending -> DotHoje
                            hasPending -> DotMes
                            allDone -> DotConcluida
                            else -> DotMes
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isSelected -> MaterialTheme.colorScheme.primary
                                        isToday -> MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        else -> MaterialTheme.colorScheme.background
                                    }
                                )
                                .clickable { onDateSelected(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = cellDay.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> MaterialTheme.colorScheme.onPrimary
                                        isToday -> MaterialTheme.colorScheme.primary
                                        isHoliday -> HolidayText
                                        else -> MaterialTheme.colorScheme.onBackground
                                    },
                                    fontSize = 13.sp
                                )
                                // Indicator dots row
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (hasTasks) {
                                        Box(Modifier.size(5.dp).clip(CircleShape).background(dotColor))
                                    }
                                    if (isHoliday && !isSelected) {
                                        Box(Modifier.size(5.dp).clip(CircleShape).background(HolidayText))
                                    }
                                }
                            }
                        }
                        dayIndex++
                    }
                }
            }
        }
    }
}
