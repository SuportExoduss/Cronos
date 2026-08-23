package com.exoduss.cronos.ui.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File

/**
 * Dialog exibido ao concluir uma tarefa perguntando se o usuário quer adicionar fotos/vídeos.
 * [onMediaPicked] recebe (uri: String, mediaType: "photo"|"video")
 */
@Composable
fun AddMediaDialog(
    taskId: String,
    onMediaPicked: (uri: String, mediaType: String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCameraUri?.toString()?.let { uri ->
                onMediaPicked(uri, "photo")
            }
        }
        pendingCameraUri = null
        onDismiss()
    }

    // Gallery launcher (images + videos)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            // Persist permission to read the URI across sessions
            context.contentResolver.takePersistableUriPermission(
                it, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            val type = if (context.contentResolver.getType(it)?.startsWith("video") == true)
                "video" else "photo"
            onMediaPicked(it.toString(), type)
        }
        onDismiss()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Photo, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Registrar conquista?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        },
        text = {
            Text(
                "Adicione fotos ou vídeos desta tarefa concluída. Eles ficarão salvos no histórico e poderão ser incluídos no backup.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Câmera
                Button(
                    onClick = {
                        val uri = createCameraUri(context, taskId)
                        pendingCameraUri = uri
                        cameraLauncher.launch(uri)
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(Icons.Default.CameraAlt, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Abrir câmera", fontWeight = FontWeight.SemiBold)
                }
                // Galeria
                OutlinedButton(
                    onClick = { galleryLauncher.launch("image/* video/*") },
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Icon(Icons.Default.Photo, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Escolher da galeria")
                }
                // Pular
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Agora não",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        dismissButton = {}
    )
}

private fun createCameraUri(context: Context, taskId: String): Uri {
    val file = File(context.externalCacheDir, "cronos_${taskId}_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}
