package com.exoduss.cronos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exoduss.cronos.notifications.AlarmReceiver
import com.exoduss.cronos.ui.navigation.CronosNavGraph
import com.exoduss.cronos.ui.theme.CronosTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    // taskId recebido via notificação — reativo para cold start e onNewIntent
    private val pendingNotifTaskId = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingNotifTaskId.value = intent?.getStringExtra(AlarmReceiver.EXTRA_TASK_ID)
        enableEdgeToEdge()
        setContent {
            val startDestination by viewModel.startDestination.collectAsStateWithLifecycle()
            val darkMode by viewModel.darkMode.collectAsStateWithLifecycle()
            val useSystem by viewModel.useSystemTheme.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()

            val isDark = if (useSystem) systemDark else darkMode

            CronosTheme(darkTheme = isDark) {
                if (startDestination != null) {
                    CronosNavGraph(
                        startDestination = startDestination!!,
                        pendingNotifTaskId = pendingNotifTaskId.value,
                        onNotifTaskConsumed = { pendingNotifTaskId.value = null },
                        onThemeChange = { /* handled by ViewModel + PreferencesRepository */ }
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    // Warm start: app já aberto em background, nova notificação chegou
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        pendingNotifTaskId.value = intent.getStringExtra(AlarmReceiver.EXTRA_TASK_ID)
    }
}
