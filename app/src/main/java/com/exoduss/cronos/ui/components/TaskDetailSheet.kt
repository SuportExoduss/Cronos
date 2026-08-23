package com.exoduss.cronos.ui.components

import android.net.Uri
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.exoduss.cronos.domain.model.*
import com.exoduss.cronos.ui.theme.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailSheet(
    visible: Boolean,
    task: Task?,
    subtasks: List<Subtask>,
    taskType: TaskType?,
    media: List<TaskMedia> = emptyList(),
    showMediaPrompt: Boolean = false,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onUpdateStatus: (TaskStatus) -> Unit,
    onToggleSubtask: (String, Boolean) -> Unit,
    onAddMedia: (uri: String, mediaType: String) -> Unit = { _, _ -> },
    onDeleteMedia: (TaskMedia) -> Unit = {},
    onMediaPromptDismiss: () -> Unit = {}
) {
    if (!visible || task == null) return

    var showDeleteDialog    by remember { mutableStateOf(false) }
    var showAddMediaDialog  by remember { mutableStateOf(false) }
    var fullScreenMedia     by remember { mutableStateOf<TaskMedia?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val typeColor = taskType?.color() ?: TypePessoal
    val isDone = task.status == TaskStatus.DONE

    // Aciona o dialog de mídia quando vem de fora (ao concluir)
    LaunchedEffect(showMediaPrompt) {
        if (showMediaPrompt) showAddMediaDialog = true
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.KeyboardArrowDown, "Fechar")
                }
                Spacer(Modifier.weight(1f))
                if (isDone) {
                    IconButton(onClick = { showAddMediaDialog = true }) {
                        Icon(Icons.Default.AddAPhoto, "Adicionar foto",
                            tint = MaterialTheme.colorScheme.primary)
                    }
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Editar",
                        tint = MaterialTheme.colorScheme.primary)
                }
            }

            // Badge tipo
            taskType?.let { type ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(type.color().copy(alpha = 0.18f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(type.name, style = MaterialTheme.typography.labelMedium, color = type.color())
                }
                Spacer(Modifier.height(8.dp))
            }

            // Título
            Text(
                text = task.title,
                style = MaterialTheme.typography.headlineSmall,
                textDecoration = if (isDone) TextDecoration.LineThrough else null,
                color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant
                        else MaterialTheme.colorScheme.onSurface
            )

            if (task.description.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(task.description, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                task.dueDate?.let { date ->
                    InfoChip(Icons.Default.CalendarMonth,
                        date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        isAlert = task.isOverdue())
                }
                task.dueTime?.let { time ->
                    InfoChip(Icons.Default.Schedule,
                        time.format(DateTimeFormatter.ofPattern("HH:mm")))
                }
                InfoChip(Icons.Default.Flag, task.priority.label,
                    color = task.priority.priorityColor())
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (task.recurrence != Recurrence.NONE)
                    InfoChip(Icons.Default.Repeat, task.recurrence.label)
                if (task.reminderEnabled)
                    InfoChip(Icons.Default.NotificationsActive, "Lembrete")
                if (task.pinned)
                    InfoChip(Icons.Default.PushPin, "Fixada")
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(Modifier.height(16.dp))

            // Status
            Text("Status", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()) {
                TaskStatus.entries.forEach { status ->
                    FilterChip(
                        selected = task.status == status,
                        onClick = { onUpdateStatus(status) },
                        label = { Text(status.label, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Subtarefas
            if (subtasks.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(Modifier.height(12.dp))
                val doneCount = subtasks.count { it.isDone }
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text("Subtarefas", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$doneCount/${subtasks.size}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { if (subtasks.isEmpty()) 0f else doneCount.toFloat() / subtasks.size },
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(8.dp))
                subtasks.forEach { sub ->
                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()) {
                        Checkbox(checked = sub.isDone,
                            onCheckedChange = { onToggleSubtask(sub.id, it) })
                        Text(sub.title, style = MaterialTheme.typography.bodyMedium,
                            textDecoration = if (sub.isDone) TextDecoration.LineThrough else null,
                            color = if (sub.isDone) MaterialTheme.colorScheme.onSurfaceVariant
                                    else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            // ── Galeria de mídia (apenas tarefas concluídas) ──
            if (isDone) {
                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                Spacer(Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.PhotoLibrary, null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp))
                        Text("Fotos & Vídeos",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (media.isNotEmpty()) {
                            Text("(${media.size})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    TextButton(
                        onClick = { showAddMediaDialog = true },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.Add, null, Modifier.size(14.dp))
                        Spacer(Modifier.width(2.dp))
                        Text("Adicionar", style = MaterialTheme.typography.labelSmall)
                    }
                }

                if (media.isEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.AddPhotoAlternate, null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(4.dp))
                            Text("Nenhuma mídia adicionada",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    Spacer(Modifier.height(8.dp))
                    // Column + chunked: compatível com verticalScroll sem altura fixa
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        media.chunked(3).forEach { rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                rowItems.forEach { item ->
                                    MediaThumb(
                                        media = item,
                                        onClick = { fullScreenMedia = item },
                                        onDelete = { onDeleteMedia(item) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                // Preenche espaço vazio na última linha incompleta
                                repeat(3 - rowItems.size) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            Spacer(Modifier.height(8.dp))

            Text(
                text = "Criado em ${
                    java.time.Instant.ofEpochMilli(task.createdAt)
                        .atZone(java.time.ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy",
                            java.util.Locale("pt", "BR")))
                }",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            TextButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Delete, null, Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Excluir tarefa")
            }
        }
    }

    // Dialog de adicionar mídia
    if (showAddMediaDialog) {
        AddMediaDialog(
            taskId = task.id,
            onMediaPicked = { uri, type ->
                onAddMedia(uri, type)
                showAddMediaDialog = false
                if (showMediaPrompt) onMediaPromptDismiss()
            },
            onDismiss = {
                showAddMediaDialog = false
                if (showMediaPrompt) onMediaPromptDismiss()
            }
        )
    }

    // Fullscreen viewer
    fullScreenMedia?.let { item ->
        MediaFullScreen(media = item, onDismiss = { fullScreenMedia = null })
    }

    // Confirmar exclusão
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir esta tarefa?") },
            text = { Text("Esta ação não pode ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = { showDeleteDialog = false; onDelete() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

// ── Miniatura de mídia ─────────────────────────────────────

@Composable
private fun MediaThumb(
    media: TaskMedia,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showConfirm by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(Uri.parse(media.uri))
                .crossfade(true)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        // Ícone de vídeo
        if (media.mediaType == "video") {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.PlayArrow, null,
                    tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
        // Indicador de backup pendente
        if (media.pendingBackup) {
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .align(Alignment.TopStart)
                    .padding(3.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.CloudUpload, null,
                    tint = Color.White, modifier = Modifier.size(10.dp))
            }
        }
        // Botão excluir
        IconButton(
            onClick = { showConfirm = true },
            modifier = Modifier
                .size(26.dp)
                .align(Alignment.TopEnd)
                .padding(2.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.45f))
        ) {
            Icon(Icons.Default.Close, null, tint = Color.White,
                modifier = Modifier.size(14.dp))
        }
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Remover mídia?") },
            confirmButton = {
                TextButton(onClick = { showConfirm = false; onDelete() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error)) { Text("Remover") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Cancelar") }
            }
        )
    }
}

// ── Visualizador fullscreen ────────────────────────────────

@Composable
private fun MediaFullScreen(media: TaskMedia, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(Uri.parse(media.uri))
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Close, "Fechar", tint = Color.White)
            }
            if (media.pendingBackup) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.CloudUpload, null,
                        tint = Color.White, modifier = Modifier.size(14.dp))
                    Text("Pendente de backup",
                        color = Color.White, fontSize = 12.sp)
                }
            }
        }
    }
}

// ── InfoChip (reutilizado) ─────────────────────────────────

@Composable
private fun InfoChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isAlert: Boolean = false,
    color: Color = Color.Unspecified
) {
    val chipColor = when {
        isAlert -> ErrorLight
        color != Color.Unspecified -> color
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(shape = RoundedCornerShape(20.dp), color = chipColor.copy(alpha = 0.12f)) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, null, Modifier.size(12.dp), tint = chipColor)
            Text(label, style = MaterialTheme.typography.labelSmall, color = chipColor)
        }
    }
}
