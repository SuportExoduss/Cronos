package com.exoduss.cronos.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.exoduss.cronos.MainActivity
import com.exoduss.cronos.R
import com.exoduss.cronos.ui.theme.BackgroundDark
import com.exoduss.cronos.ui.theme.CronosTheme
import com.exoduss.cronos.ui.theme.GoldPrimary
import com.exoduss.cronos.ui.theme.MutedDark
import kotlinx.coroutines.delay

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CronosTheme {
                SplashScreen(onFinish = { navigateToMain() })
            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

@Composable
private fun SplashScreen(onFinish: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    var skipped by remember { mutableStateOf(false) }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "alpha"
    )
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.85f,
        animationSpec = tween(durationMillis = 600),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        visible = true
        delay(2000)
        if (!skipped) onFinish()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                skipped = true
                onFinish()
            },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alpha)
                .scale(scale)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_splash),
                contentDescription = "Cronos",
                modifier = Modifier.size(160.dp)
            )

            Spacer(Modifier.height(28.dp))

            Text(
                "CRONOS",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Black,
                color = GoldPrimary,
                letterSpacing = 6.sp
            )

            Spacer(Modifier.height(8.dp))

            Text(
                "Organize seu tempo",
                style = MaterialTheme.typography.bodyLarge,
                color = MutedDark
            )
        }

        Text(
            "Toque para continuar",
            style = MaterialTheme.typography.bodySmall,
            color = MutedDark.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 56.dp)
                .alpha(alpha)
        )
    }
}
