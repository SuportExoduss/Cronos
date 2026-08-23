package com.exoduss.cronos.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.exoduss.cronos.MainActivity
import com.exoduss.cronos.R

object NotificationHelper {

    const val CHANNEL_REMINDERS = "task_reminders"
    const val CHANNEL_DAILY = "daily_summary"
    const val DAILY_SUMMARY_NOTIF_ID = 99999

    fun createChannels(context: Context) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_REMINDERS,
                "Lembretes de Tarefas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificações de lembrete para tarefas agendadas"
                enableVibration(true)
            }
        )
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_DAILY,
                "Resumo Diário",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Resumo diário das suas tarefas pendentes"
            }
        )
    }

    fun showTaskReminder(context: Context, notifId: Int, taskId: String, taskTitle: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(AlarmReceiver.EXTRA_TASK_ID, taskId)
        }
        val pi = PendingIntent.getActivity(
            context, notifId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(context, CHANNEL_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(taskTitle)
            .setContentText("Você tem uma tarefa pendente!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(notifId, notif)
    }

    fun showDailySummary(context: Context, pendingCount: Int, overdueCount: Int) {
        if (pendingCount == 0 && overdueCount == 0) return

        val body = buildString {
            if (overdueCount > 0) append("$overdueCount atrasada(s)")
            if (overdueCount > 0 && pendingCount > 0) append(" · ")
            if (pendingCount > 0) append("$pendingCount pendente(s) hoje")
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pi = PendingIntent.getActivity(
            context, DAILY_SUMMARY_NOTIF_ID, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notif = NotificationCompat.Builder(context, CHANNEL_DAILY)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Resumo do dia — Cronos")
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pi)
            .setAutoCancel(true)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(DAILY_SUMMARY_NOTIF_ID, notif)
    }
}
