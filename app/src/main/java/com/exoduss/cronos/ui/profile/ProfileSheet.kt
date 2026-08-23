package com.exoduss.cronos.ui.profile

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.exoduss.cronos.domain.model.CronosUser
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSheet(
    userName: String,
    googleConnected: Boolean,
    userPlan: String,
    lastBackupTime: Long,
    onDismiss: () -> Unit,
    onConnectGoogle: () -> Unit,
    onBackup: () -> Unit,
    user: CronosUser? = null,
    onSignInClick: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onRestore: () -> Unit = {},
    profilePhoto: Bitmap? = null
) {
    val planFeatures = listOf(
        "Backup ilimitado na nuvem",
        "Seleção de horário para salvamento",
        "Backup automático agendado",
        "Tarefas em grupo"
    )
    val plans = listOf(
        Triple("Basics", "R$ 8,00/mês", planFeatures),
        Triple("Waves",  "R$ 6,50/mês", planFeatures),
        Triple("Storm",  "R$ 4,99/mês", planFeatures)
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // ── Profile header ────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    if (profilePhoto != null) {
                        Image(
                            bitmap = profilePhoto.asImageBitmap(),
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().clip(CircleShape)
                        )
                    } else {
                        Text(
                            text = userName.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userName.ifBlank { "Usuário" },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    if (user != null) {
                        Text(
                            user.email ?: "Conta Google conectada",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(2.dp))
                        TextButton(
                            onClick = onSignOut,
                            contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                        ) {
                            Icon(Icons.Default.Logout, null, Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Sair", style = MaterialTheme.typography.labelSmall)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onSignInClick,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(Icons.Default.Login, null, Modifier.size(15.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Entrar com Google", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // ── Current plan ──────────────────────────────────
            Text("Plano atual",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 14.dp, vertical = 7.dp)
            ) {
                Text(
                    userPlan.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // ── Backup no Google Drive ────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Default.CloudUpload, null,
                    tint = if (user == null) MaterialTheme.colorScheme.outline
                           else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp))
                Text("Backup no Google Drive",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(6.dp))
            if (user == null) {
                Text("Entre com sua conta Google para ativar o backup na nuvem.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                val lastBackupLabel = if (lastBackupTime == 0L) "Nenhum backup realizado ainda"
                else {
                    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR"))
                    "Último backup: ${sdf.format(Date(lastBackupTime))}"
                }
                Text(lastBackupLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()) {
                    FilledTonalButton(onClick = onBackup, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.CloudUpload, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Backup", style = MaterialTheme.typography.labelMedium)
                    }
                    OutlinedButton(onClick = onRestore, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.CloudDownload, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Restaurar", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            HorizontalDivider()
            Spacer(Modifier.height(16.dp))

            // ── Plan upgrade cards ────────────────────────────
            Text("Fazer upgrade",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))

            plans.forEach { (name, price, features) ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            features.forEach { feature ->
                                Row(verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 1.dp)) {
                                    Icon(Icons.Default.Check, null,
                                        Modifier.size(12.dp),
                                        tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(4.dp))
                                    Text(feature,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Text(price,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Text(
                                    "Em breve",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
