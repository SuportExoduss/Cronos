package com.exoduss.cronos.data.repository

import androidx.room.withTransaction
import com.exoduss.cronos.data.local.CronosDatabase
import com.exoduss.cronos.data.local.dao.SubtaskDao
import com.exoduss.cronos.data.local.dao.TaskDao
import com.exoduss.cronos.data.local.entity.toEntity
import com.exoduss.cronos.domain.model.Subtask
import com.exoduss.cronos.domain.model.Task
import com.exoduss.cronos.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.LocalDate.now
import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val subtaskDao: SubtaskDao,
    private val database: CronosDatabase
) : TaskRepository {

    override fun getAllTasks(): Flow<List<Task>> =
        taskDao.getAllTasks().map { it.map { e -> e.toDomain() } }

    override fun getPendingTasks(): Flow<List<Task>> =
        taskDao.getPendingTasks().map { it.map { e -> e.toDomain() } }

    override fun getTasksByDate(date: LocalDate): Flow<List<Task>> =
        taskDao.getTasksByDate(date.toString()).map { it.map { e -> e.toDomain() } }

    override suspend fun getTaskById(id: String): Task? =
        taskDao.getTaskById(id)?.toDomain()

    override suspend fun saveTask(task: Task) =
        taskDao.upsertTask(task.toEntity())

    override suspend fun saveTaskWithSubtasks(task: Task, subtasks: List<Subtask>) =
        database.withTransaction {
            taskDao.upsertTask(task.toEntity())
            subtaskDao.deleteSubtasksForTask(task.id)
            subtasks.forEach { subtaskDao.upsertSubtask(it.toEntity()) }
        }

    override suspend fun getTasksWithActiveReminders(): List<Task> =
        taskDao.getTasksWithActiveReminders(now().toString()).map { it.toDomain() }

    override suspend fun deleteTask(task: Task) =
        taskDao.deleteTask(task.toEntity())

    override suspend fun updateStatus(id: String, status: TaskStatus) =
        taskDao.updateStatus(id, status.name, System.currentTimeMillis())

    override suspend fun togglePin(id: String, pinned: Boolean) =
        taskDao.updatePinned(id, pinned, System.currentTimeMillis())
}
