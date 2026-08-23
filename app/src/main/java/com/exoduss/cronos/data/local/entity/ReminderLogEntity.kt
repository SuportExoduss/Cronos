package com.exoduss.cronos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.exoduss.cronos.domain.model.ReminderLog

@Entity(tableName = "reminder_logs")
data class ReminderLogEntity(
    @PrimaryKey val id: String,
    val taskId: String,
    val scheduledTime: Long,
    val alarmId: Int,
    val status: String
) {
    fun toDomain() = ReminderLog(id = id, taskId = taskId, scheduledTime = scheduledTime, alarmId = alarmId, status = status)
}

fun ReminderLog.toEntity() = ReminderLogEntity(id = id, taskId = taskId, scheduledTime = scheduledTime, alarmId = alarmId, status = status)
