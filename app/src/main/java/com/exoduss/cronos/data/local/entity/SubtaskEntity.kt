package com.exoduss.cronos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.exoduss.cronos.domain.model.Subtask

@Entity(tableName = "subtasks")
data class SubtaskEntity(
    @PrimaryKey val id: String,
    val taskId: String,
    val title: String,
    val isDone: Boolean,
    val `order`: Int
) {
    fun toDomain() = Subtask(id = id, taskId = taskId, title = title, isDone = isDone, order = order)
}

fun Subtask.toEntity() = SubtaskEntity(id = id, taskId = taskId, title = title, isDone = isDone, order = order)
