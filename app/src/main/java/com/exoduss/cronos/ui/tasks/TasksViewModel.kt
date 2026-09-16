package com.exoduss.cronos.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exoduss.cronos.data.repository.TaskRepository
import com.exoduss.cronos.data.repository.TaskTypeRepository
import com.exoduss.cronos.domain.model.Task
import com.exoduss.cronos.domain.model.TaskStatus
import com.exoduss.cronos.domain.model.TaskType
import com.exoduss.cronos.domain.model.isOverdue
import com.exoduss.cronos.domain.model.smartGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class TasksViewModel @Inject constructor(
    taskRepository: TaskRepository,
    taskTypeRepository: TaskTypeRepository
) : ViewModel() {

    val taskTypes: StateFlow<List<TaskType>> = taskTypeRepository.getAllTypes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow<TaskStatus?>(null)
    val statusFilter = _statusFilter.asStateFlow()

    private val _typeFilter = MutableStateFlow<String?>(null)
    val typeFilter = _typeFilter.asStateFlow()

    private val allTasks = taskRepository.getAllTasks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredTasks: StateFlow<List<Task>> = combine(
        allTasks, _searchQuery, _statusFilter, _typeFilter
    ) { tasks, query, status, typeId ->
        tasks
            .filter { task ->
                val matchQuery = query.isBlank() ||
                    task.title.contains(query, ignoreCase = true) ||
                    task.description.contains(query, ignoreCase = true)
                val matchStatus = when (status) {
                    null -> true
                    TaskStatus.PENDING -> task.status == TaskStatus.PENDING          // inclui atrasadas
                    TaskStatus.IN_PROGRESS -> task.status == TaskStatus.IN_PROGRESS
                    TaskStatus.DONE -> task.status == TaskStatus.DONE
                    TaskStatus.CANCELLED -> task.status == TaskStatus.CANCELLED
                }
                val matchType = typeId == null || task.typeId == typeId
                matchQuery && matchStatus && matchType
            }
            .sortedWith(compareBy({ it.smartGroup() }, { it.dueDate?.toString() ?: "9999" }, { it.createdAt }))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val overdueCount: StateFlow<Int> = allTasks
        .map { tasks -> tasks.count { it.isOverdue() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setSearch(query: String) { _searchQuery.value = query }
    fun setStatusFilter(status: TaskStatus?) { _statusFilter.value = status }
    fun setTypeFilter(typeId: String?) { _typeFilter.value = typeId }
}
