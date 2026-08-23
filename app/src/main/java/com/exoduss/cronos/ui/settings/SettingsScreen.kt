package com.exoduss.cronos.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.exoduss.cronos.domain.model.DefaultTaskTypes
import com.exoduss.cronos.domain.model.TaskType
import java.time.LocalDate
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onThemeChange: (Boolean) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val darkMode by viewModel.darkMode.collectAsStateWithLifecycle()
    val useSystem by viewModel.useSystemTheme.collectAsStateWithLifecycle()
    val dailySummary by viewModel.dailySummaryEnabled.collectAsStateWithLifecycle()
    val backupMedia by viewModel.backupMediaEnabled.collectAsStateWithLifecycle()
    val taskTypes by viewModel.taskTypes.collectAsStateWithLifecycle()
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val backupMessage by viewModel.backupMessage.collectAsStateWithLifecycle()

    var nameInput by remember(userName) { mutableStateOf(userName) }
    var showSaved by remember { mutableStateOf(false) }
    var showTypeUsedAlert by remember { mutableStateOf(false) }
    var showAddTypeDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri -> uri?.let { viewModel.exportTo(it) } }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri -> uri?.let { viewModel.importFrom(it) } }

    if (backupMessage != null) {
        LaunchedEffect(backupMessage) {
            kotlinx.coroutines.delay(3500)
            viewModel.clearBackupMessage()
        }
    }

    if (showSaved) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            showSaved = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Perfil
            SettingsCard(icon = Icons.Default.Person, title = "Perfil") {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Seu nome") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    leadingIcon = { Icon(Icons.Default.Badge, null) }
                )
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.updateUserName(nameInput); showSaved = true },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = nameInput != userName
                ) {
                    Icon(Icons.Default.Save, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Salvar")
                }
                if (showSaved) {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Default.CheckCircle, null,
                            tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                        Text("Salvo!", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Aparência
            SettingsCard(icon = Icons.Default.Palette, title = "Aparência") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Seguir sistema", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = useSystem, onCheckedChange = { viewModel.setUseSystemTheme(it) })
                }
                if (!useSystem) {
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Modo escuro", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = darkMode,
                            onCheckedChange = {
                                viewModel.setDarkMode(it)
                                onThemeChange(it)
                            }
                        )
                    }
                }
            }

            // Notificações
            SettingsCard(icon = Icons.Default.Notifications, title = "Notificações") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Resumo diário", style = MaterialTheme.typography.bodyMedium)
                        Text("Notificação diária às 08h", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = dailySummary, onCheckedChange = { viewModel.setDailySummaryEnabled(it) })
                }
            }

            // Backup
            SettingsCard(icon = Icons.Default.Backup, title = "Backup") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Backup de mídias visuais", style = MaterialTheme.typography.bodyMedium)
                        Text("Incluir fotos e vídeos no backup do Drive",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = backupMedia, onCheckedChange = { viewModel.setBackupMediaEnabled(it) })
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(Modifier.height(16.dp))

                Text("Backup local (arquivo)", style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium)
                Text("Exporte ou importe todos os seus dados em um arquivo .json. O backup no Google Drive usará a mesma base.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { exportLauncher.launch("cronos-backup-${LocalDate.now()}.json") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Upload, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Exportar")
                    }
                    OutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("application/json")) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Download, null, Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Importar")
                    }
                }
                if (backupMessage != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        backupMessage!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Tipos de tarefa
            SettingsCard(icon = Icons.Default.Category, title = "Tipos de Tarefa") {
                taskTypes.forEach { type ->
                    val typeColor = try {
                        Color(android.graphics.Color.parseColor(type.color))
                    } catch (e: Exception) { MaterialTheme.colorScheme.primary }

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(typeColor))
                        Spacer(Modifier.width(12.dp))
                        Text(type.name, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        if (!type.isDefault) {
                            IconButton(
                                onClick = {
                                    viewModel.deleteTaskType(type, onUsed = { showTypeUsedAlert = true })
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Delete, null,
                                    tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showAddTypeDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Criar tipo personalizado")
                }
            }

            // Estatísticas
            SettingsCard(icon = Icons.Default.BarChart, title = "Estatísticas") {
                InfoRow("Total de tarefas", stats.total.toString())
                InfoRow("Pendentes", stats.pending.toString())
                InfoRow("Concluídas", stats.done.toString())
                InfoRow("Taxa de conclusão", "${stats.completionRate}%")
            }

            // Sobre
            SettingsCard(icon = Icons.Default.Info, title = "Sobre") {
                InfoRow("Versão", "1.0.0")
                InfoRow("Desenvolvido por", "Exoduss")
                InfoRow("Plataforma", "Android")
            }

            Spacer(Modifier.height(80.dp))
        }
    }

    if (showTypeUsedAlert) {
        AlertDialog(
            onDismissRequest = { showTypeUsedAlert = false },
            title = { Text("Tipo em uso") },
            text = { Text("Este tipo está sendo usado por tarefas e não pode ser excluído.") },
            confirmButton = {
                TextButton(onClick = { showTypeUsedAlert = false }) { Text("OK") }
            }
        )
    }

    if (showAddTypeDialog) {
        AddTypeDialog(
            onDismiss = { showAddTypeDialog = false },
            onSave = { name, color ->
                viewModel.saveTaskType(
                    TaskType(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        color = color,
                        icon = "label",
                        isDefault = false
                    )
                )
                showAddTypeDialog = false
            }
        )
    }
}

@Composable
private fun SettingsCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun AddTypeDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    val colorOptions = listOf(
        "#2E6DB4", "#27AE60", "#C0392B", "#8E44AD", "#E67E22",
        "#16A085", "#2C3E50", "#D35400", "#F39C12", "#1ABC9C"
    )
    var selectedColor by remember { mutableStateOf(colorOptions[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo tipo de tarefa") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Text("Cor", style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(colorOptions.take(5), colorOptions.drop(5)).forEach { rowColors ->
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            rowColors.forEach { hex ->
                                val color = try {
                                    Color(android.graphics.Color.parseColor(hex))
                                } catch (e: Exception) { Color.Gray }
                                val isSelected = selectedColor == hex
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                        .then(
                                            if (isSelected) Modifier.border(3.dp, Color.White.copy(alpha = 0.9f), CircleShape)
                                            else Modifier
                                        )
                                        .clickable { selectedColor = hex },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(Icons.Default.Check, null,
                                            tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onSave(name, selectedColor) },
                enabled = name.isNotBlank()) { Text("Criar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
