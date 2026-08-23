package com.exoduss.cronos.data.backup

import com.exoduss.cronos.data.local.entity.SubtaskEntity
import com.exoduss.cronos.data.local.entity.TaskEntity
import com.exoduss.cronos.data.local.entity.TaskMediaEntity
import com.exoduss.cronos.data.local.entity.TaskTypeEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class BackupSerializerTest {

    private val serializer = BackupSerializer()

    private fun sampleData() = BackupData(
        exportedAt = 1_700_000_000_000L,
        tasks = listOf(
            TaskEntity(
                id = "t1", title = "Pagar conta", description = "luz",
                typeId = "trabalho", priority = "HIGH", status = "PENDING",
                dueDate = "2026-06-20", dueTime = "14:30", recurrence = "MONTHLY",
                pinned = true, reminderEnabled = true, reminderFrequency = 3,
                reminderDaysBefore = 2, isGroupTask = false, groupId = null,
                createdAt = 1L, updatedAt = 2L
            ),
            TaskEntity(
                id = "t2", title = "Sem data", description = "",
                typeId = null, priority = "LOW", status = "DONE",
                dueDate = null, dueTime = null, recurrence = "NONE",
                pinned = false, reminderEnabled = false, reminderFrequency = 1,
                reminderDaysBefore = 0, isGroupTask = false, groupId = null,
                createdAt = 3L, updatedAt = 4L
            )
        ),
        subtasks = listOf(SubtaskEntity("s1", "t1", "Anexar boleto", false, 0)),
        taskTypes = listOf(TaskTypeEntity("custom1", "Projeto X", "#123456", "label", false)),
        media = listOf(TaskMediaEntity("m1", "t1", "content://x", "photo", 5L, true))
    )

    @Test
    fun roundTrip_preserves_all_fields() {
        val original = sampleData()
        val restored = serializer.fromJson(serializer.toJson(original))

        assertEquals(original.version, restored.version)
        assertEquals(original.exportedAt, restored.exportedAt)
        assertEquals(original.tasks, restored.tasks)
        assertEquals(original.subtasks, restored.subtasks)
        assertEquals(original.taskTypes, restored.taskTypes)
        assertEquals(original.media, restored.media)
    }

    @Test
    fun roundTrip_preserves_nullable_fields() {
        val restored = serializer.fromJson(serializer.toJson(sampleData()))
        val t2 = restored.tasks.first { it.id == "t2" }
        assertEquals(null, t2.typeId)
        assertEquals(null, t2.dueDate)
        assertEquals(null, t2.dueTime)
    }

    @Test
    fun emptyBackup_roundTrips() {
        val restored = serializer.fromJson(serializer.toJson(BackupData(exportedAt = 0L)))
        assertTrue(restored.tasks.isEmpty())
        assertTrue(restored.subtasks.isEmpty())
        assertTrue(restored.taskTypes.isEmpty())
        assertTrue(restored.media.isEmpty())
    }

    @Test
    fun corruptedJson_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException::class.java) {
            serializer.fromJson("{ not valid json ]")
        }
    }

    @Test
    fun futureVersion_isRejected() {
        val future = """{"version": 9999, "exportedAt": 0, "tasks": [], "subtasks": [], "taskTypes": [], "media": []}"""
        assertThrows(IllegalArgumentException::class.java) {
            serializer.fromJson(future)
        }
    }
}
