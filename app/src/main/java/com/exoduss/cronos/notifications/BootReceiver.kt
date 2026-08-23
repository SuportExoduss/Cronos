package com.exoduss.cronos.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.exoduss.cronos.data.repository.PreferencesRepository
import com.exoduss.cronos.data.repository.TaskRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var taskRepository: TaskRepository
    @Inject lateinit var reminderScheduler: ReminderScheduler
    @Inject lateinit var preferencesRepository: PreferencesRepository

    // Scope com SupervisorJob: falha em um filho não cancela os demais
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED
            && intent.action != "android.intent.action.QUICKBOOT_POWERON"
        ) return

        val pendingResult = goAsync()
        scope.launch {
            try {
                // Usa query específica — evita carregar todas as tarefas em memória
                taskRepository.getTasksWithActiveReminders()
                    .forEach { reminderScheduler.scheduleReminders(it) }

                // Reagenda resumo diário
                val summaryEnabled = preferencesRepository.dailySummaryEnabled.first()
                val summaryHour    = preferencesRepository.dailySummaryHour.first()
                if (summaryEnabled) reminderScheduler.scheduleDailySummary(summaryHour)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
