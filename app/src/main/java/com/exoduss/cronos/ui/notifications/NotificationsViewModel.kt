package com.exoduss.cronos.ui.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exoduss.cronos.data.repository.TaskRepository
import com.exoduss.cronos.data.repository.TaskTypeRepository
import com.exoduss.cronos.domain.model.Task
import com.exoduss.cronos.domain.model.TaskStatus
import com.exoduss.cronos.domain.model.TaskType
import com.exoduss.cronos.util.CurrentDateProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class NotificationsViewModel @Inject constructor(
    taskRepository: TaskRepository,
    taskTypeRepository: TaskTypeRepository,
    currentDateProvider: CurrentDateProvider
) : ViewModel() {

    val taskTypes: StateFlow<List<TaskType>> = taskTypeRepository.getAllTypes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val pendingTasks = taskRepository.getPendingTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Data reativa compartilhada — reemite na virada da meia-noite (ver CurrentDateProvider).
    private val currentDate = currentDateProvider.currentDate

    val overdueTasks: StateFlow<List<Task>> = currentDate
        .flatMapLatest { today ->
            pendingTasks.map { tasks -> tasks.filter { it.dueDate != null && it.dueDate < today } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayTasks: StateFlow<List<Task>> = currentDate
        .flatMapLatest { today ->
            pendingTasks.map { tasks -> tasks.filter { it.dueDate == today } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val upcomingTasks: StateFlow<List<Task>> = currentDate
        .flatMapLatest { today ->
            pendingTasks.map { tasks ->
                tasks.filter { it.dueDate != null && it.dueDate > today }
                    .sortedBy { it.dueDate }
                    .take(15)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
