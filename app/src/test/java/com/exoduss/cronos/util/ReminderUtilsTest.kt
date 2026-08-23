package com.exoduss.cronos.util

import org.junit.Assert.*
import org.junit.Test

class ReminderUtilsTest {

    @Test
    fun freq1_returns_dueHour() {
        assertEquals(listOf(14), ReminderUtils.reminderHours(1, dueHour = 14))
    }

    @Test
    fun freq1_defaults_to_9_when_no_dueHour() {
        assertEquals(listOf(9), ReminderUtils.reminderHours(1))
    }

    @Test
    fun freq2_returns_2_distinct_hours_in_range() {
        val result = ReminderUtils.reminderHours(2)
        assertEquals(2, result.size)
        assertEquals(result.distinct().size, result.size)
        assertTrue(result.all { it in 6..22 })
    }

    @Test
    fun freq9_covers_all_9_even_hours() {
        val result = ReminderUtils.reminderHours(9)
        assertEquals(listOf(6, 8, 10, 12, 14, 16, 18, 20, 22), result)
    }

    @Test
    fun freq10_returns_10_distinct_hours() {
        val result = ReminderUtils.reminderHours(10)
        assertEquals(10, result.size)
        assertEquals(result.distinct().size, result.size)
        assertTrue(result.all { it in 6..22 })
    }

    @Test
    fun freq12_returns_12_distinct_hours() {
        val result = ReminderUtils.reminderHours(12)
        assertEquals(12, result.size)
        assertEquals(result.distinct().size, result.size)
        assertTrue(result.all { it in 6..22 })
    }

    @Test
    fun freq_above_12_clamped_to_12() {
        val result = ReminderUtils.reminderHours(99)
        assertEquals(12, result.size)
    }

    @Test
    fun freq_below_1_clamped_to_1_returning_default_hour() {
        val result = ReminderUtils.reminderHours(0)
        assertEquals(listOf(9), result)
    }

    @Test
    fun hours_are_sorted_for_all_frequencies() {
        for (freq in 1..12) {
            val result = ReminderUtils.reminderHours(freq)
            assertEquals("freq=$freq should be sorted", result.sorted(), result)
        }
    }

    @Test
    fun freq10_to_12_use_odd_hours_not_half_past() {
        val validHours = ((6..22 step 2) + (7..21 step 2)).toSet()
        for (freq in 10..12) {
            val result = ReminderUtils.reminderHours(freq)
            assertTrue(
                "freq=$freq: all hours must be whole-hour slots",
                result.all { it in validHours }
            )
        }
    }
}
