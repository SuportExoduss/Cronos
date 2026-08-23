package com.exoduss.cronos.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exoduss.cronos.data.repository.TaskRepository
import com.exoduss.cronos.data.repository.TaskTypeRepository
import com.exoduss.cronos.domain.model.Task
import com.exoduss.cronos.domain.model.TaskStatus
import com.exoduss.cronos.domain.model.TaskType
import com.exoduss.cronos.domain.model.smartGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

enum class CalendarFilter { MONTH, WEEK, DAY }

@HiltViewModel
class CalendarViewModel @Inject constructor(
    taskRepository: TaskRepository,
    taskTypeRepository: TaskTypeRepository
) : ViewModel() {

    val taskTypes: StateFlow<List<TaskType>> = taskTypeRepository.getAllTypes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _currentMonth = MutableStateFlow(YearMonth.now())
    val currentMonth: StateFlow<YearMonth> = _currentMonth.asStateFlow()

    private val _calendarFilter = MutableStateFlow(CalendarFilter.DAY)
    val calendarFilter: StateFlow<CalendarFilter> = _calendarFilter.asStateFlow()

    val allTasks: StateFlow<List<Task>> = taskRepository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTasks: StateFlow<List<Task>> = combine(
        allTasks, _selectedDate, _calendarFilter
    ) { tasks, date, filter ->
        val today = LocalDate.now()
        val filtered = when (filter) {
            CalendarFilter.DAY -> tasks.filter { it.dueDate == date }
            CalendarFilter.WEEK -> {
                val start = date.with(java.time.DayOfWeek.MONDAY)
                val end = start.plusDays(6)
                tasks.filter { it.dueDate != null && !it.dueDate.isBefore(start) && !it.dueDate.isAfter(end) }
            }
            CalendarFilter.MONTH -> tasks.filter {
                it.dueDate?.let { d -> d.year == _currentMonth.value.year && d.monthValue == _currentMonth.value.monthValue } == true
            }
        }
        filtered.sortedWith(compareBy({ it.smartGroup() }, { it.dueTime?.toString() ?: "" }, { it.createdAt }))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectDate(date: LocalDate) { _selectedDate.value = date }
    fun previousMonth() { _currentMonth.update { it.minusMonths(1) } }
    fun nextMonth() { _currentMonth.update { it.plusMonths(1) } }
    fun setFilter(filter: CalendarFilter) { _calendarFilter.value = filter }
}
