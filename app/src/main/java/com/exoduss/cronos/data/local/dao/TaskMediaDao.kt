package com.exoduss.cronos.data.local.dao

import androidx.room.*
import com.exoduss.cronos.data.local.entity.TaskMediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskMediaDao {

    @Query("SELECT * FROM task_media WHERE taskId = :taskId ORDER BY createdAt ASC")
    fun getMediaForTask(taskId: String): Flow<List<TaskMediaEntity>>

    /** Snapshot único (não-reativo) — usado pelo backup/export. */
    @Query("SELECT * FROM task_media")
    suspend fun getAllSnapshot(): List<TaskMediaEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(media: TaskMediaEntity)

    @Delete
    suspend fun delete(media: TaskMediaEntity)

    @Query("DELETE FROM task_media WHERE taskId = :taskId")
    suspend fun deleteForTask(taskId: String)

    @Query("SELECT * FROM task_media WHERE pendingBackup = 1")
    suspend fun getPendingBackup(): List<TaskMediaEntity>

    @Query("UPDATE task_media SET pendingBackup = 0 WHERE id = :id")
    suspend fun markBackedUp(id: String)
}
