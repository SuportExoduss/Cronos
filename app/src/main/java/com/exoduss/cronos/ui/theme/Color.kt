package com.exoduss.cronos.ui.theme

import androidx.compose.ui.graphics.Color

// ── Fundos — estrutura do app ─────────────────────────────────────────────────
val BackgroundDark  = Color(0xFF0B0D12)   // bg principal — sem preto puro
val BackgroundLight = Color(0xFFF4F6F8)

val CardDark  = Color(0xFF1C212B)          // surface / cards
val CardLight = Color(0xFFFFFFFF)

val SurfaceDark  = Color(0xFF12151C)       // bg secundário
val SurfaceLight = Color(0xFFEDF0F3)

val SurfaceElevatedDark  = Color(0xFF232938)  // camadas elevated
val SurfaceElevatedLight = Color(0xFFE2E6EC)

// ── Verde Cronos — identidade principal ──────────────────────────────────────
val PrimaryDark  = Color(0xFF22C55E)   // verde cronos
val PrimaryLight = Color(0xFF16A34A)   // verde pressed / hover

val OnPrimaryDark  = Color(0xFFFFFFFF)
val OnPrimaryLight = Color(0xFFFFFFFF)

val GreenGlow   = Color(0xFF4ADE80)    // brilho leve
val GreenBorder = Color(0x3322C55E)    // borda transparente (#22C55E20)

// containers para MaterialTheme
val PrimaryContainerDark  = Color(0xFF14532D)
val PrimaryContainerLight = Color(0xFFDCFCE7)

// ── Dourado Cronos — valor / premium ─────────────────────────────────────────
val GoldPrimary = Color(0xFFD4AF37)
val GoldLight   = Color(0xFFFACC15)
val GoldDark    = Color(0xFFA16207)
val GoldSurface = Color(0x1AD4AF37)    // #D4AF3720

// secondary herda o dourado
val SecondaryDark  = Color(0xFFD4AF37)
val SecondaryLight = Color(0xFFA16207)

// ── Azul Temporal — sistema / eventos ────────────────────────────────────────
val BluePrimary = Color(0xFF38BDF8)
val BlueLight   = Color(0xFF7DD3FC)
val BlueGlow    = Color(0x2038BDF8)    // #38BDF820

val AccentDark  = Color(0xFF38BDF8)
val AccentLight = Color(0xFF0EA5E9)

// ── Texto ─────────────────────────────────────────────────────────────────────
val OnBackgroundDark  = Color(0xFFE5E7EB)  // texto principal
val OnBackgroundLight = Color(0xFF111827)

val MutedDark  = Color(0xFF9CA3AF)         // texto secundário
val MutedLight = Color(0xFF6B7280)

val TextWeakDark  = Color(0xFF6B7280)      // texto fraco
val TextWeakLight = Color(0xFF9CA3AF)

val TextDisabledDark  = Color(0xFF4B5563)
val TextDisabledLight = Color(0xFFD1D5DB)

// ── Bordas ────────────────────────────────────────────────────────────────────
val BorderDark  = Color(0xFF2A2F3A)
val BorderLight = Color(0xFFD1D5DB)

// ── Tab bar ──────────────────────────────────────────────────────────────────
val TabBarDark  = Color(0xFF0B0D12)
val TabBarLight = Color(0xFFFFFFFF)

// ── Semânticas de status ──────────────────────────────────────────────────────
val SuccessDark  = Color(0xFF22C55E)
val SuccessLight = Color(0xFF22C55E)   // mesmo valor — usado direto em componentes

val WarningDark  = Color(0xFFF59E0B)
val WarningLight = Color(0xFFD97706)

val ErrorDark  = Color(0xFFEF4444)
val ErrorLight = Color(0xFFEF4444)     // mesmo valor — usado direto em componentes

// ── Calendário ───────────────────────────────────────────────────────────────
val DotConcluida = Color(0xFF22C55E)
val DotHoje      = Color(0xFFD4AF37)   // dourado
val DotMes       = Color(0xFF38BDF8)   // azul temporal
val DotAtrasada  = Color(0xFFEF4444)

// ── Tipos de tarefa ───────────────────────────────────────────────────────────
val TypeTrabalho = Color(0xFF38BDF8)   // azul temporal
val TypePessoal  = Color(0xFF22C55E)   // verde cronos
val TypeSaude    = Color(0xFFEF4444)   // vermelho
val TypeEstudo   = Color(0xFF9C6FD6)   // violeta
val TypeFamilia  = Color(0xFFD4AF37)   // dourado

// ── Prioridades ───────────────────────────────────────────────────────────────
val PriorityLow    = Color(0xFF22C55E)
val PriorityMedium = Color(0xFFF59E0B)
val PriorityHigh   = Color(0xFFEF4444)
