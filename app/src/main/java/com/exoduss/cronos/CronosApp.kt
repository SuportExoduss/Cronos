package com.exoduss.cronos

import android.app.Application
import com.exoduss.cronos.notifications.NotificationHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CronosApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createChannels(this)
    }
}
