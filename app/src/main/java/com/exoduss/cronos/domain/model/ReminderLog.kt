package com.exoduss.cronos.domain.model

import java.util.UUID

data class ReminderLog(
    val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    val scheduledTime: Long,
    val alarmId: Int,
    val status: String = "pending"
)
