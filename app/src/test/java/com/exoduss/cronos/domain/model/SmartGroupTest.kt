package com.exoduss.cronos.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class SmartGroupTest {

    private fun task(
        status: TaskStatus = TaskStatus.PENDING,
        pinned: Boolean = false,
        dueDate: LocalDate? = null
    ) = Task(title = "Test", status = status, pinned = pinned, dueDate = dueDate)

    @Test
    fun done_task_is_group_5() {
        assertEquals(5, task(status = TaskStatus.DONE).smartGroup())
    }

    @Test
    fun cancelled_task_is_group_5() {
        assertEquals(5, task(status = TaskStatus.CANCELLED).smartGroup())
    }

    @Test
    fun pinned_active_task_is_group_0() {
        assertEquals(0, task(pinned = true, dueDate = LocalDate.now().plusDays(1)).smartGroup())
    }

    @Test
    fun pinned_task_with_no_date_is_group_0() {
        assertEquals(0, task(pinned = true).smartGroup())
    }

    @Test
    fun overdue_task_is_group_1() {
        assertEquals(1, task(dueDate = LocalDate.now().minusDays(1)).smartGroup())
    }

    @Test
    fun task_due_today_is_group_2() {
        assertEquals(2, task(dueDate = LocalDate.now()).smartGroup())
    }

    @Test
    fun task_with_no_date_is_group_3() {
        assertEquals(3, task(dueDate = null).smartGroup())
    }

    @Test
    fun future_task_is_group_4() {
        assertEquals(4, task(dueDate = LocalDate.now().plusDays(3)).smartGroup())
    }

    @Test
    fun done_pinned_is_group_5_not_0() {
        // DONE/CANCELLED status takes priority over pinned flag
        assertEquals(5, task(status = TaskStatus.DONE, pinned = true).smartGroup())
    }

    @Test
    fun cancelled_pinned_is_group_5_not_0() {
        assertEquals(5, task(status = TaskStatus.CANCELLED, pinned = true).smartGroup())
    }
}
