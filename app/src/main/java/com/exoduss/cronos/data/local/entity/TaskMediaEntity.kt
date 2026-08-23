package com.exoduss.cronos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_media")
data class TaskMediaEntity(
    @PrimaryKey val id: String,
    val taskId: String,
    val uri: String,
    val mediaType: String,       // "photo" | "video"
    val createdAt: Long,
    val pendingBackup: Boolean = true
)
