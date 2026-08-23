package com.exoduss.cronos.domain.model

data class TaskMedia(
    val id: String,
    val taskId: String,
    val uri: String,
    val mediaType: String,   // "photo" | "video"
    val createdAt: Long,
    val pendingBackup: Boolean = true
)
