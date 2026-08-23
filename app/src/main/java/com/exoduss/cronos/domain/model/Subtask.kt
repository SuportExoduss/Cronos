package com.exoduss.cronos.domain.model

import java.util.UUID

data class Subtask(
    val id: String = UUID.randomUUID().toString(),
    val taskId: String,
    val title: String,
    val isDone: Boolean = false,
    val order: Int = 0
)
