package com.exoduss.cronos.data.local.dao

import androidx.room.*
import com.exoduss.cronos.data.local.entity.SubtaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubtaskDao {

    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY `order` ASC")
    fun getSubtasksForTask(taskId: String): Flow<List<SubtaskEntity>>

    /** Snapshot único (não-reativo) — usado pelo backup/export. */
    @Query("SELECT * FROM subtasks")
    suspend fun getAllSnapshot(): List<SubtaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSubtask(subtask: SubtaskEntity)

    @Delete
    suspend fun deleteSubtask(subtask: SubtaskEntity)

    @Query("DELETE FROM subtasks WHERE taskId = :taskId")
    suspend fun deleteSubtasksForTask(taskId: String)

    @Query("UPDATE subtasks SET isDone = :isDone WHERE id = :id")
    suspend fun toggleSubtask(id: String, isDone: Boolean)
}
