package com.exoduss.cronos.data.repository

import com.exoduss.cronos.domain.model.Subtask
import com.exoduss.cronos.domain.model.Task
import com.exoduss.cronos.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TaskRepository {
    fun getAllTasks(): Flow<List<Task>>
    fun getPendingTasks(): Flow<List<Task>>
    fun getTasksByDate(date: LocalDate): Flow<List<Task>>
    suspend fun getTaskById(id: String): Task?
    suspend fun saveTask(task: Task)
    suspend fun saveTaskWithSubtasks(task: Task, subtasks: List<Subtask>)
    suspend fun getTasksWithActiveReminders(): List<Task>
    suspend fun deleteTask(task: Task)
    suspend fun updateStatus(id: String, status: TaskStatus)
    suspend fun togglePin(id: String, pinned: Boolean)
    suspend fun markNextSpawned(id: String)
}
