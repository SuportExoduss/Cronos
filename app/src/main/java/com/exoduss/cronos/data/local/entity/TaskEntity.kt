package com.exoduss.cronos.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.exoduss.cronos.domain.model.Priority
import com.exoduss.cronos.domain.model.Recurrence
import com.exoduss.cronos.domain.model.Task
import com.exoduss.cronos.domain.model.TaskStatus
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

private val TIME_FMT = DateTimeFormatter.ofPattern("HH:mm")

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val typeId: String?,
    val priority: String,
    val status: String,
    val dueDate: String?,
    val dueTime: String?,
    val recurrence: String,
    val pinned: Boolean,
    val reminderEnabled: Boolean,
    val reminderFrequency: Int = 1,
    val reminderDaysBefore: Int = 0,
    val isGroupTask: Boolean,
    val groupId: String?,
    @ColumnInfo(defaultValue = "0") val nextSpawned: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain() = Task(
        id          = id,
        title       = title,
        description = description,
        typeId      = typeId,
        priority    = runCatching { Priority.valueOf(priority) }
                          .getOrElse { Priority.MEDIUM },
        status      = runCatching { TaskStatus.valueOf(status) }
                          .getOrElse { TaskStatus.PENDING },
        dueDate     = runCatching { dueDate?.let { LocalDate.parse(it) } }
                          .getOrElse { null },
        dueTime     = runCatching { dueTime?.let { LocalTime.parse(it, TIME_FMT) } }
                          .getOrElse { null },
        recurrence  = runCatching { Recurrence.valueOf(recurrence) }
                          .getOrElse { Recurrence.NONE },
        pinned             = pinned,
        reminderEnabled    = reminderEnabled,
        reminderFrequency  = reminderFrequency,
        reminderDaysBefore = reminderDaysBefore,
        isGroupTask = isGroupTask,
        groupId     = groupId,
        nextSpawned = nextSpawned,
        createdAt   = createdAt,
        updatedAt   = updatedAt
    )
}

fun Task.toEntity() = TaskEntity(
    id = id,
    title = title,
    description = description,
    typeId = typeId,
    priority = priority.name,
    status = status.name,
    dueDate = dueDate?.toString(),
    dueTime = dueTime?.format(TIME_FMT),
    recurrence = recurrence.name,
    pinned = pinned,
    reminderEnabled = reminderEnabled,
    reminderFrequency = reminderFrequency,
    reminderDaysBefore = reminderDaysBefore,
    isGroupTask = isGroupTask,
    groupId = groupId,
    nextSpawned = nextSpawned,
    createdAt = createdAt,
    updatedAt = updatedAt
)
