package com.exoduss.cronos.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exoduss.cronos.data.repository.PreferencesRepository
import com.exoduss.cronos.data.repository.TaskRepository
import com.exoduss.cronos.data.repository.TaskTypeRepository
import com.exoduss.cronos.domain.model.Task
import com.exoduss.cronos.domain.model.TaskType
import com.exoduss.cronos.domain.model.smartGroup
import com.exoduss.cronos.util.CurrentDateProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    taskRepository: TaskRepository,
    taskTypeRepository: TaskTypeRepository,
    preferencesRepository: PreferencesRepository,
    currentDateProvider: CurrentDateProvider
) : ViewModel() {

    val userName: StateFlow<String> = preferencesRepository.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val taskTypes: StateFlow<List<TaskType>> = taskTypeRepository.getAllTypes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val currentDate = currentDateProvider.currentDate

    // Tarefas de hoje, com ordenação inteligente — reage automaticamente à virada da meia-noite
    val todayTasks: StateFlow<List<Task>> = currentDate
        .flatMapLatest { date ->
            taskRepository.getTasksByDate(date).map { tasks ->
                tasks.sortedWith(compareBy({ it.smartGroup() }, { it.dueTime?.toString() ?: "" }, { it.createdAt }))
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Uma única query para tarefas pendentes — pendingTasks e overdueTasks derivam dela
    private val _allPending: Flow<List<Task>> = taskRepository.getPendingTasks()

    // Tarefas pendentes (sem data ou futuras) — máx. 5 exibidas na Home; reage à virada da meia-noite
    val pendingTasks: StateFlow<List<Task>> = currentDate
        .flatMapLatest { today ->
            _allPending.map { tasks ->
                tasks
                    .filter { it.dueDate == null || it.dueDate > today }
                    .sortedBy { it.dueDate }
                    .take(5)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Tarefas atrasadas — derivadas do mesmo flow, sem segunda query ao banco; reage à meia-noite
    val overdueTasks: StateFlow<List<Task>> = currentDate
        .flatMapLatest { today ->
            _allPending.map { tasks ->
                tasks.filter { it.dueDate != null && it.dueDate < today }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
