package com.exoduss.cronos.data.local.dao

import androidx.room.*
import com.exoduss.cronos.data.local.entity.TaskTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskTypeDao {

    @Query("SELECT * FROM task_types ORDER BY isDefault DESC, name ASC")
    fun getAllTypes(): Flow<List<TaskTypeEntity>>

    /** Snapshot único (não-reativo) — usado pelo backup/export. */
    @Query("SELECT * FROM task_types")
    suspend fun getAllSnapshot(): List<TaskTypeEntity>

    /** Restore: sobrescreve tipos (inclusive personalizados editados). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTypes(types: List<TaskTypeEntity>)

    @Query("SELECT * FROM task_types WHERE id = :id")
    suspend fun getTypeById(id: String): TaskTypeEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertType(type: TaskTypeEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTypes(types: List<TaskTypeEntity>)

    @Update
    suspend fun updateType(type: TaskTypeEntity)

    @Delete
    suspend fun deleteType(type: TaskTypeEntity)

    @Query("SELECT COUNT(*) FROM tasks WHERE typeId = :typeId")
    suspend fun getTaskCountForType(typeId: String): Int

    @Query("SELECT COUNT(*) FROM task_types")
    suspend fun count(): Int
}
