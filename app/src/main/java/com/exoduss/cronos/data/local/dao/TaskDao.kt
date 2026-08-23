package com.exoduss.cronos.data.local.dao

import androidx.room.*
import com.exoduss.cronos.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    /** Snapshot único (não-reativo) — usado pelo backup/export. */
    @Query("SELECT * FROM tasks")
    suspend fun getAllSnapshot(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE dueDate = :date ORDER BY dueTime ASC, createdAt ASC")
    fun getTasksByDate(date: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE status NOT IN ('DONE', 'CANCELLED') ORDER BY dueDate ASC, createdAt DESC")
    fun getPendingTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: String): TaskEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("UPDATE tasks SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: Long)

    @Query("UPDATE tasks SET pinned = :pinned, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updatePinned(id: String, pinned: Boolean, updatedAt: Long)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("""
        SELECT * FROM tasks
        WHERE reminderEnabled = 1
        AND dueDate IS NOT NULL
        AND status NOT IN ('DONE', 'CANCELLED')
        AND dueDate >= :today
    """)
    suspend fun getTasksWithActiveReminders(today: String): List<TaskEntity>
}
