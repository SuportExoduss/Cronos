package com.exoduss.cronos.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.exoduss.cronos.data.repository.PreferencesRepository
import com.exoduss.cronos.data.repository.TaskRepository
import com.exoduss.cronos.domain.model.TaskStatus
import com.exoduss.cronos.domain.model.isOverdue
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class DailySummaryReceiver : BroadcastReceiver() {

    @Inject lateinit var taskRepository: TaskRepository
    @Inject lateinit var preferencesRepository: PreferencesRepository
    @Inject lateinit var reminderScheduler: ReminderScheduler

    // Scope com SupervisorJob: falha em um filho não cancela os demais
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_DAILY_SUMMARY) return
        val pendingResult = goAsync()
        scope.launch {
            try {
                val tasks   = taskRepository.getAllTasks().first()
                val today   = LocalDate.now()
                val pending = tasks.count { it.status == TaskStatus.PENDING && it.dueDate == today }
                val overdue = tasks.count { it.isOverdue() }
                NotificationHelper.showDailySummary(context, pending, overdue)

                // Re-agenda o alarme exato para o mesmo horário no dia seguinte
                val hour = preferencesRepository.dailySummaryHour.first()
                reminderScheduler.scheduleDailySummary(hour)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_DAILY_SUMMARY = "com.exoduss.cronos.ACTION_DAILY_SUMMARY"
    }
}
