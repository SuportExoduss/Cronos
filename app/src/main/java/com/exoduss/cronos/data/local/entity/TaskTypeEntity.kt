package com.exoduss.cronos.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.exoduss.cronos.domain.model.TaskType

@Entity(tableName = "task_types")
data class TaskTypeEntity(
    @PrimaryKey val id: String,
    val name: String,
    val color: String,
    val icon: String,
    val isDefault: Boolean
) {
    fun toDomain() = TaskType(id = id, name = name, color = color, icon = icon, isDefault = isDefault)
}

fun TaskType.toEntity() = TaskTypeEntity(id = id, name = name, color = color, icon = icon, isDefault = isDefault)
