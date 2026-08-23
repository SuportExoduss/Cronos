package com.exoduss.cronos.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.exoduss.cronos.domain.model.Task
import com.exoduss.cronos.domain.model.TaskStatus
import com.exoduss.cronos.util.ReminderUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val alarmManager: AlarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Agenda todos os lembretes para a tarefa:
     *  - Lembretes na frequência escolhida, TODOS OS DIAS a partir de hoje até a data da tarefa
     *  - 3 lembretes obrigatórios no dia anterior: 08h, 14h, 20h
     */
    fun scheduleReminders(task: Task) {
        cancelReminders(task.id)

        if (!task.reminderEnabled
            || task.dueDate == null
            || task.status == TaskStatus.DONE
            || task.status == TaskStatus.CANCELLED
        ) return

        val now    = System.currentTimeMillis()
        val today  = LocalDate.now()
        val due    = task.dueDate

        // Não agenda nada se a tarefa já venceu
        if (due.isBefore(today)) return

        val hours = ReminderUtils.reminderHours(task.reminderFrequency, task.dueTime?.hour ?: 9)

        // Data de início: `reminderDaysBefore` dias antes do vencimento, mas nunca antes de hoje
        val startDay = maxOf(today, due.minusDays(task.reminderDaysBefore.toLong()))

        // ── Lembretes diários: startDay → vencimento (máx. MAX_DAYS dias) ────
        var alarmIndex = 0
        var day = startDay
        val maxDay = if (due.isBefore(startDay.plusDays(MAX_DAYS.toLong())))
            due else startDay.plusDays(MAX_DAYS.toLong())

        while (!day.isAfter(maxDay)) {
            hours.forEach { hour ->
                val triggerMs = day.atTime(hour, 0)
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                if (triggerMs > now) {
                    setAlarm(triggerMs, buildIntent(task.id, task.title, alarmIdFor(task.id, alarmIndex)))
                }
                alarmIndex++
            }
            day = day.plusDays(1)
        }

        // ── 3 lembretes obrigatórios: 1 dia antes (08h / 14h / 20h) ─────────
        // Usam índices 500–502 para nunca colidir com os diários (0–372 max — ver MAX_DAILY_INDEX)
        val dayBefore = due.minusDays(1)
        if (!dayBefore.isBefore(today)) {
            listOf(8, 14, 20).forEachIndexed { i, hour ->
                val triggerMs = dayBefore.atTime(hour, 0)
                    .atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
                if (triggerMs > now) {
                    val title = "${task.title} — amanhã!"
                    setAlarm(triggerMs, buildIntent(task.id, title, alarmIdFor(task.id, 500 + i)))
                }
            }
        }
    }

    /**
     * Cancela TODOS os alarmes registrados para a tarefa.
     * Usa FLAG_NO_CREATE para não criar PendingIntents que não existem.
     */
    fun cancelReminders(taskId: String) {
        // Cobre TODO o range de índices que scheduleReminders pode ter usado.
        // O range é inclusivo (0..MAX_DAILY_INDEX) — ver justificativa em MAX_DAILY_INDEX.
        for (i in 0..MAX_DAILY_INDEX) {
            buildCancelIntent(taskId, alarmIdFor(taskId, i))?.let { alarmManager.cancel(it) }
        }
        for (i in 500..502) {
            buildCancelIntent(taskId, alarmIdFor(taskId, i))?.let { alarmManager.cancel(it) }
        }
    }

    /**
     * Agenda o próximo resumo diário usando alarme exato.
     * O receiver re-agenda automaticamente para o dia seguinte após disparar.
     * Chamadas múltiplas são seguras — FLAG_UPDATE_CURRENT substitui o anterior.
     */
    fun scheduleDailySummary(hour: Int) {
        // FLAG_UPDATE_CURRENT nunca retorna null — elvis por segurança de compilação
        val pi = buildDailySummaryPendingIntent(PendingIntent.FLAG_UPDATE_CURRENT) ?: return
        val trigger = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        setAlarm(trigger.timeInMillis, pi)
    }

    /** Cancela o resumo diário. */
    fun cancelDailySummary() {
        buildDailySummaryPendingIntent(PendingIntent.FLAG_NO_CREATE)
            ?.let { alarmManager.cancel(it) }
    }

    // ── internos ───────────────────────────────────────────────────────────

    private fun setAlarm(triggerMs: Long, pi: PendingIntent) {
        // Alarmes exatos requerem permissão SCHEDULE_EXACT_ALARM a partir do Android 12.
        // A PermissionsScreen orienta o usuário a conceder a permissão, mas se ela não
        // estiver disponível usamos alarme inexato como fallback seguro.
        val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                       alarmManager.canScheduleExactAlarms()
        if (canExact) {
            try {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerMs, pi)
                return
            } catch (_: SecurityException) { /* permissão revogada em runtime — cai no fallback */ }
        }
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMs, pi)
    }

    private fun buildIntent(taskId: String, taskTitle: String, alarmId: Int): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_REMINDER
            putExtra(AlarmReceiver.EXTRA_TASK_ID, taskId)
            putExtra(AlarmReceiver.EXTRA_TASK_TITLE, taskTitle)
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        return PendingIntent.getBroadcast(
            context, alarmId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * ID único por (taskId, índice).
     * Usa os últimos 8 caracteres do UUID para reduzir correlação de hash,
     * com espaço de 600 slots por tarefa (max index=502 < 600 → sem overlap).
     * 0x3FFFFF = 4.194.303 → × 600 = 2.516.582.400 < Int.MAX → sem overflow.
     */
    private fun alarmIdFor(taskId: String, index: Int): Int {
        val hash = taskId.takeLast(8).hashCode()
        return (hash and 0x3FFFFF) * 600 + index
    }

    /** PendingIntent para cancelamento — usa FLAG_NO_CREATE para não criar se não existir. */
    private fun buildCancelIntent(taskId: String, alarmId: Int): PendingIntent? {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_REMINDER
            putExtra(AlarmReceiver.EXTRA_TASK_ID, taskId)
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }
        return PendingIntent.getBroadcast(
            context, alarmId, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** PendingIntent para o resumo diário — flags configuráveis para create/update/no-create. */
    private fun buildDailySummaryPendingIntent(extraFlags: Int): PendingIntent? {
        val intent = Intent(context, DailySummaryReceiver::class.java)
            .setAction(DailySummaryReceiver.ACTION_DAILY_SUMMARY)
        return PendingIntent.getBroadcast(
            context, DAILY_SUMMARY_REQUEST, intent,
            extraFlags or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        /** Máximo de dias para agendamento antecipado (evita exceder o limite do AlarmManager). */
        private const val MAX_DAYS = 30

        /** Máximo de lembretes diários por tarefa (freq é coerçido em 1..12 no TaskFormViewModel). */
        private const val MAX_HOURS_PER_DAY = 12

        /**
         * Maior índice de alarme diário que scheduleReminders pode gerar.
         * O loop percorre startDay..maxDay de forma INCLUSIVA (até MAX_DAYS + 1 dias) e
         * incrementa alarmIndex por slot de hora — logo (MAX_DAYS + 1) * MAX_HOURS_PER_DAY = 372.
         * cancelReminders DEVE cobrir todo esse range, senão alarmes nos índices superiores
         * (361–372) ficam órfãos e disparam notificações fantasma após conclusão/exclusão.
         */
        private const val MAX_DAILY_INDEX = (MAX_DAYS + 1) * MAX_HOURS_PER_DAY  // 372

        private const val DAILY_SUMMARY_REQUEST = 99998
    }
}
