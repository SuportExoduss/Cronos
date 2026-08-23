package com.exoduss.cronos.ui.profile

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ProfileIcon(viewModel: ProfileViewModel = hiltViewModel()) {
    val userName        by viewModel.userName.collectAsStateWithLifecycle()
    val googleConnected by viewModel.googleConnected.collectAsStateWithLifecycle()
    val userPlan        by viewModel.userPlan.collectAsStateWithLifecycle()
    val lastBackupTime  by viewModel.lastBackupTime.collectAsStateWithLifecycle()
    val currentUser     by viewModel.currentUser.collectAsStateWithLifecycle()
    val authMessage     by viewModel.authMessage.collectAsStateWithLifecycle()
    val profilePhoto    by viewModel.profilePhoto.collectAsStateWithLifecycle()
    var showSheet       by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val signInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> viewModel.completeSignIn(result.data) }

    // Autorização incremental do Drive: lança o consentimento sob demanda e retoma a ação.
    val driveConsentIntent by viewModel.driveConsentIntent.collectAsStateWithLifecycle()
    val consentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result -> viewModel.onDriveConsentResult(result.resultCode == Activity.RESULT_OK) }

    LaunchedEffect(driveConsentIntent) {
        driveConsentIntent?.let { consentLauncher.launch(it) }
    }

    LaunchedEffect(authMessage) {
        authMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearAuthMessage()
        }
    }

    IconButton(onClick = { showSheet = true }) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (profilePhoto != null) {
                Image(
                    bitmap = profilePhoto!!.asImageBitmap(),
                    contentDescription = "Foto de perfil",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(CircleShape)
                )
            } else {
                Text(
                    text       = userName.firstOrNull()?.uppercase() ?: "?",
                    style      = MaterialTheme.typography.labelLarge,
                    color      = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showSheet) {
        ProfileSheet(
            userName        = userName,
            googleConnected = googleConnected,
            userPlan        = userPlan,
            lastBackupTime  = lastBackupTime,
            onDismiss       = { showSheet = false },
            onConnectGoogle = {},
            onBackup        = { viewModel.performBackup() },
            user            = currentUser,
            onSignInClick   = { signInLauncher.launch(viewModel.signInIntent()) },
            onSignOut       = { viewModel.signOut() },
            onRestore       = { viewModel.restoreFromDrive() },
            profilePhoto    = profilePhoto
        )
    }
}
