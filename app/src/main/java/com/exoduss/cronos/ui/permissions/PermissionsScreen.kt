package com.exoduss.cronos.ui.permissions

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exoduss.cronos.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionsViewModel @Inject constructor(
    private val prefs: PreferencesRepository
) : ViewModel() {
    fun markPermissionsDone() {
        viewModelScope.launch { prefs.setPermissionsRequested() }
    }
}

@Composable
fun PermissionsScreen(
    onDone: () -> Unit,
    viewModel: PermissionsViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // Notification permission state (Android 13+)
    var notifGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    // Battery optimization state
    var batteryGranted by remember {
        mutableStateOf(
            context.getSystemService<PowerManager>()
                ?.isIgnoringBatteryOptimizations(context.packageName) == true
        )
    }

    // Exact alarm state (Android 12+)
    var alarmGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (context.getSystemService(android.app.AlarmManager::class.java))
                    ?.canScheduleExactAlarms() == true
            } else true
        )
    }

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> notifGranted = granted }

    val batteryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        batteryGranted = context.getSystemService<PowerManager>()
            ?.isIgnoringBatteryOptimizations(context.packageName) == true
    }

    val alarmLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        alarmGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (context.getSystemService(android.app.AlarmManager::class.java))
                ?.canScheduleExactAlarms() == true
        } else true
    }

    val allGranted = notifGranted && batteryGranted && alarmGranted

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Security, null,
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(48.dp))
        }

        Spacer(Modifier.height(28.dp))

        Text(
            "Permissões necessárias",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Para que o Cronos funcione corretamente, precisamos de acesso a alguns recursos do seu dispositivo.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(36.dp))

        // Notifications
        PermissionRow(
            icon = Icons.Default.Notifications,
            title = "Notificações",
            description = "Alertas de tarefas e resumo diário",
            granted = notifGranted,
            onRequest = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        )

        Spacer(Modifier.height(12.dp))

        // Battery optimization
        PermissionRow(
            icon = Icons.Default.BatteryFull,
            title = "Execução em segundo plano",
            description = "Manter lembretes ativos com a tela desligada",
            granted = batteryGranted,
            onRequest = {
                val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:${context.packageName}")
                }
                batteryLauncher.launch(intent)
            }
        )

        Spacer(Modifier.height(12.dp))

        // Exact alarms
        PermissionRow(
            icon = Icons.Default.Alarm,
            title = "Alarmes exatos",
            description = "Lembretes na hora certa, sem atrasos",
            granted = alarmGranted,
            onRequest = {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                        data = Uri.parse("package:${context.packageName}")
                    }
                    alarmLauncher.launch(intent)
                }
            }
        )

        Spacer(Modifier.weight(1f))

        if (!allGranted) {
            Text(
                "Sem essas permissões algumas funções podem não funcionar corretamente.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(12.dp))
        }

        Button(
            onClick = {
                viewModel.markPermissionsDone()
                onDone()
            },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            enabled = allGranted
        ) {
            Text(if (allGranted) "Continuar" else "Conceda todas as permissões")
        }

        Spacer(Modifier.height(8.dp))

        TextButton(
            onClick = {
                viewModel.markPermissionsDone()
                onDone()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Pular (funcionalidade limitada)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun PermissionRow(
    icon: ImageVector,
    title: String,
    description: String,
    granted: Boolean,
    onRequest: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (granted)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                icon, null,
                tint = if (granted) MaterialTheme.colorScheme.primary
                       else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
                Text(description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (granted) {
                Icon(Icons.Default.CheckCircle, null,
                    tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            } else {
                FilledTonalButton(
                    onClick = onRequest,
                    modifier = Modifier.height(34.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text("Permitir", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
