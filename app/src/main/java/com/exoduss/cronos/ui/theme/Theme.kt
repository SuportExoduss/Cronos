package com.exoduss.cronos.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary                = PrimaryDark,           // #22C55E verde cronos
    onPrimary              = OnPrimaryDark,          // branco
    primaryContainer       = PrimaryContainerDark,  // #14532D
    onPrimaryContainer     = GreenGlow,             // #4ADE80
    secondary              = GoldPrimary,           // #D4AF37 dourado
    onSecondary            = Color(0xFF0B0D12),
    secondaryContainer     = GoldSurface,           // #D4AF3720
    onSecondaryContainer   = GoldLight,             // #FACC15
    tertiary               = BluePrimary,           // #38BDF8 azul temporal
    onTertiary             = Color(0xFF0B0D12),
    tertiaryContainer      = BlueGlow,              // #38BDF820
    onTertiaryContainer    = BlueLight,             // #7DD3FC
    background             = BackgroundDark,        // #0B0D12
    onBackground           = OnBackgroundDark,      // #E5E7EB
    surface                = CardDark,              // #1C212B
    onSurface              = OnBackgroundDark,
    surfaceVariant         = SurfaceElevatedDark,   // #232938
    onSurfaceVariant       = MutedDark,             // #9CA3AF
    surfaceContainer       = SurfaceDark,           // #12151C
    surfaceContainerHigh   = SurfaceElevatedDark,
    outline                = BorderDark,            // #2A2F3A
    outlineVariant         = Color(0xFF1E2430),
    error                  = ErrorDark,             // #EF4444
    onError                = OnPrimaryDark,
    errorContainer         = Color(0xFF7F1D1D),
    onErrorContainer       = Color(0xFFFCA5A5),
)

private val LightColorScheme = lightColorScheme(
    primary                = PrimaryLight,          // #16A34A
    onPrimary              = OnPrimaryLight,
    primaryContainer       = PrimaryContainerLight, // #DCFCE7
    onPrimaryContainer     = Color(0xFF14532D),
    secondary              = SecondaryLight,        // #A16207 ouro escuro
    onSecondary            = OnPrimaryLight,
    secondaryContainer     = Color(0xFFFEF9C3),
    onSecondaryContainer   = GoldDark,
    tertiary               = AccentLight,           // #0EA5E9
    onTertiary             = OnPrimaryLight,
    tertiaryContainer      = Color(0xFFE0F2FE),
    onTertiaryContainer    = Color(0xFF0369A1),
    background             = BackgroundLight,
    onBackground           = OnBackgroundLight,
    surface                = CardLight,
    onSurface              = OnBackgroundLight,
    surfaceVariant         = SurfaceElevatedLight,
    onSurfaceVariant       = MutedLight,
    surfaceContainer       = SurfaceLight,
    surfaceContainerHigh   = SurfaceElevatedLight,
    outline                = BorderLight,
    outlineVariant         = Color(0xFFE5E7EB),
    error                  = ErrorLight,
    onError                = OnPrimaryLight,
    errorContainer         = Color(0xFFFEE2E2),
    onErrorContainer       = Color(0xFF7F1D1D),
)

@Composable
fun CronosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        shapes      = CronosShapes,
        content     = content
    )
}
