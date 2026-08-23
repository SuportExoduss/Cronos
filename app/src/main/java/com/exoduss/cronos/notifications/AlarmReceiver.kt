package com.exoduss.cronos.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_REMINDER) return
        val taskId    = intent.getStringExtra(EXTRA_TASK_ID) ?: return
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Tarefa"
        val alarmId   = intent.getIntExtra(EXTRA_ALARM_ID, taskId.hashCode())
        NotificationHelper.showTaskReminder(context, alarmId, taskId, taskTitle)
    }

    companion object {
        const val ACTION_REMINDER   = "com.exoduss.cronos.ACTION_REMINDER"
        const val EXTRA_TASK_ID     = "extra_task_id"
        const val EXTRA_TASK_TITLE  = "extra_task_title"
        const val EXTRA_ALARM_ID    = "extra_alarm_id"
    }
}
