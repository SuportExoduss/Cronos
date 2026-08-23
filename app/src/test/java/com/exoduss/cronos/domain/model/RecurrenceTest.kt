package com.exoduss.cronos.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class RecurrenceTest {

    private fun task(
        recurrence: Recurrence,
        dueDate: LocalDate? = LocalDate.of(2026, 1, 31),
        status: TaskStatus = TaskStatus.DONE
    ) = Task(
        title = "Pagar conta",
        recurrence = recurrence,
        dueDate = dueDate,
        status = status
    )

    @Test
    fun none_returns_null() {
        assertNull(task(Recurrence.NONE).nextOccurrence())
    }

    @Test
    fun without_dueDate_returns_null() {
        assertNull(task(Recurrence.DAILY, dueDate = null).nextOccurrence())
    }

    @Test
    fun daily_advances_one_day() {
        val next = task(Recurrence.DAILY, dueDate = LocalDate.of(2026, 6, 14)).nextOccurrence()
        assertEquals(LocalDate.of(2026, 6, 15), next?.dueDate)
    }

    @Test
    fun weekly_advances_seven_days() {
        val next = task(Recurrence.WEEKLY, dueDate = LocalDate.of(2026, 6, 14)).nextOccurrence()
        assertEquals(LocalDate.of(2026, 6, 21), next?.dueDate)
    }

    @Test
    fun monthly_handles_short_month() {
        // 31/jan + 1 mês = 28/fev (java.time resolve o fim de mês)
        val next = task(Recurrence.MONTHLY, dueDate = LocalDate.of(2026, 1, 31)).nextOccurrence()
        assertEquals(LocalDate.of(2026, 2, 28), next?.dueDate)
    }

    @Test
    fun next_occurrence_is_pending_with_new_id() {
        val original = task(Recurrence.DAILY)
        val next = original.nextOccurrence()!!
        assertEquals(TaskStatus.PENDING, next.status)
        assertNotEquals(original.id, next.id)
    }

    @Test
    fun preserves_metadata() {
        val original = task(Recurrence.WEEKLY).copy(
            priority = Priority.HIGH,
            typeId = "trabalho",
            reminderEnabled = true,
            reminderFrequency = 3,
            pinned = true
        )
        val next = original.nextOccurrence()!!
        assertEquals(Priority.HIGH, next.priority)
        assertEquals("trabalho", next.typeId)
        assertEquals(true, next.reminderEnabled)
        assertEquals(3, next.reminderFrequency)
        assertEquals(true, next.pinned)
        assertEquals(Recurrence.WEEKLY, next.recurrence)
    }
}
