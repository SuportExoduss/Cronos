package com.exoduss.cronos.util

import kotlin.math.roundToInt

object ReminderUtils {

    /**
     * Returns the hours (0–23) at which reminders should fire, distributed between 06:00 and
     * 22:00 according to the requested frequency.
     *
     * freq == 1    → [dueHour] (defaults to 9)
     * freq 2–9    → spread evenly across the 9 even slots: 6,8,10,12,14,16,18,20,22
     * freq 10–12  → fill remaining from the 8 odd slots: 7,9,11,13,15,17,19,21
     */
    fun reminderHours(freq: Int, dueHour: Int = 9): List<Int> {
        val f         = freq.coerceIn(1, 12)
        val evenHours = (6..22 step 2).toList()   // 9 slots
        val oddHours  = (7..21 step 2).toList()   // 8 slots

        return when {
            f == 1 -> listOf(dueHour)
            f <= evenHours.size -> {
                val step = (evenHours.size - 1).toDouble() / (f - 1)
                (0 until f)
                    .map { i -> evenHours[(i * step).roundToInt().coerceIn(0, evenHours.lastIndex)] }
                    .distinct()
            }
            else -> {
                val allSlots = (evenHours + oddHours).sorted()   // 17 distinct slots
                val step = (allSlots.size - 1).toDouble() / (f - 1)
                (0 until f)
                    .map { i -> allSlots[(i * step).roundToInt().coerceIn(0, allSlots.lastIndex)] }
                    .distinct()
            }
        }
    }
}
