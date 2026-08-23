package com.exoduss.cronos.data.local.dao

import androidx.room.*
import com.exoduss.cronos.data.local.entity.ReminderLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ReminderLogEntity)

    @Query("SELECT * FROM reminder_logs WHERE taskId = :taskId ORDER BY scheduledTime DESC")
    fun getLogsForTask(taskId: String): Flow<List<ReminderLogEntity>>

    @Query("UPDATE reminder_logs SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("DELETE FROM reminder_logs WHERE taskId = :taskId")
    suspend fun deleteLogsForTask(taskId: String)
}
