package com.exoduss.cronos.ui.onboarding

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.exoduss.cronos.domain.model.DefaultTaskTypes
import com.exoduss.cronos.ui.profile.ProfileSheet
import com.exoduss.cronos.ui.profile.ProfileViewModel
import com.exoduss.cronos.ui.theme.*

private const val TOTAL_PAGES = 6

@Composable
fun OnboardingScreen(
    onDone: () -> Unit,
    onThemeChange: (Boolean) -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    var page            by remember { mutableIntStateOf(0) }
    var userName        by remember { mutableStateOf("") }
    var darkMode        by remember { mutableStateOf(false) }
    var favoriteTypeId  by remember { mutableStateOf(DefaultTaskTypes[1].id) }
    var showPlansSheet  by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        AnimatedContent(
            targetState = page,
            transitionSpec = { slideInHorizontally { it } togetherWith slideOutHorizontally { -it } },
            label = "onboarding"
        ) { currentPage ->
            when (currentPage) {
                0 -> PageName(userName = userName, onNameChange = { userName = it })
                1 -> PageTheme(darkMode = darkMode, onSelect = { dark ->
                    darkMode = dark
                    onThemeChange(dark)
                })
                2 -> PageFavoriteType(selectedId = favoriteTypeId, onSelect = { favoriteTypeId = it })
                3 -> PageGoogleLogin()
                4 -> PageFeatures()
                5 -> PagePlans(onVerPlanos = { showPlansSheet = true })
            }
        }

        Spacer(Modifier.weight(1f))

        // Dots
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(TOTAL_PAGES) { idx ->
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (idx == page) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline
                        )
                        .size(if (idx == page) 24.dp else 8.dp, 8.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (page > 0) {
                TextButton(onClick = { page-- }) { Text("Voltar") }
            } else {
                Spacer(Modifier.width(1.dp))
            }

            Button(
                onClick = {
                    if (page < TOTAL_PAGES - 1) {
                        page++
                    } else {
                        viewModel.completeOnboarding(userName, darkMode, favoriteTypeId)
                        onDone()
                    }
                },
                enabled = page != 0 || userName.isNotBlank(),
                modifier = Modifier.height(48.dp)
            ) {
                Text(if (page < TOTAL_PAGES - 1) "Próximo" else "Começar")
                if (page < TOTAL_PAGES - 1) {
                    Spacer(Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowForward, null, Modifier.size(18.dp))
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }

    // Plans profile sheet
    if (showPlansSheet) {
        val profileVm: ProfileViewModel = hiltViewModel()
        val uName by profileVm.userName.collectAsState()
        val gConn by profileVm.googleConnected.collectAsState()
        val plan  by profileVm.userPlan.collectAsState()
        val lastB by profileVm.lastBackupTime.collectAsState()
        ProfileSheet(
            userName = uName.ifBlank { userName },
            googleConnected = gConn,
            userPlan = plan,
            lastBackupTime = lastB,
            onDismiss = { showPlansSheet = false },
            onConnectGoogle = {},
            onBackup = { profileVm.performBackup() }
        )
    }
}

// ── Page 0: Name ──────────────────────────────────────────

@Composable
private fun PageName(userName: String, onNameChange: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, null, Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(32.dp))
        Text("Como posso te chamar?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(12.dp))
        Text("Vou personalizar sua experiência com o Cronos.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        OutlinedTextField(
            value = userName,
            onValueChange = onNameChange,
            label = { Text("Seu nome") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            leadingIcon = { Icon(Icons.Default.Person, null) }
        )
    }
}

// ── Page 1: Theme ─────────────────────────────────────────

@Composable
private fun PageTheme(darkMode: Boolean, onSelect: (Boolean) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Text("Qual tema você prefere?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))
        Text("Você pode mudar isso nas configurações.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            ThemeCard("Claro", BackgroundLight, OnBackgroundLight, !darkMode, { onSelect(false) }, Modifier.weight(1f))
            ThemeCard("Escuro", BackgroundDark, OnBackgroundDark, darkMode, { onSelect(true) }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun ThemeCard(
    label: String,
    bgColor: Color,
    textColor: Color,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    Box(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (selected) {
                Icon(Icons.Default.CheckCircle, null,
                    tint = PrimaryLight, modifier = Modifier.size(24.dp))
                Spacer(Modifier.height(8.dp))
            }
            Text(label, color = textColor, fontWeight = FontWeight.SemiBold)
        }
    }
}

// ── Page 2: Favorite Type ─────────────────────────────────

@Composable
private fun PageFavoriteType(selectedId: String, onSelect: (String) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Category, null, Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(32.dp))
        Text("Qual é seu tipo favorito?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(8.dp))
        Text("Esse tipo será pré-selecionado ao criar tarefas.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))

        val typeColors = listOf(TypeTrabalho, TypePessoal, TypeSaude, TypeEstudo, TypeFamilia)
        DefaultTaskTypes.forEachIndexed { idx, type ->
            val color = typeColors.getOrElse(idx) { TypePessoal }
            val selected = selectedId == type.id
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (selected) color.copy(alpha = 0.18f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onSelect(type.id) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
                Spacer(Modifier.width(12.dp))
                Text(type.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selected) color else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal)
                if (selected) {
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Default.Check, null, tint = color, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ── Page 3: Google (em breve) ─────────────────────────────

@Composable
private fun PageGoogleLogin() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Cloud, null, Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary)
        }
        Spacer(Modifier.height(32.dp))
        Text("Backup na nuvem",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(12.dp))
        Text(
            "Em breve, você poderá conectar sua conta Google para:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        listOf(
            "Backup automático das suas tarefas",
            "Sincronização entre dispositivos",
            "Recuperação de dados em caso de perda"
        ).forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.CheckCircle, null,
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp))
                Text(item,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(28.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Default.Info, null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp))
                Text(
                    "Esta funcionalidade estará disponível em uma próxima atualização.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── Page 4: Features ──────────────────────────────────────

private data class Feature(val icon: ImageVector, val title: String, val desc: String)

@Composable
private fun PageFeatures() {
    val features = listOf(
        Feature(Icons.Default.CheckBox,       "Gestão de Tarefas",      "Crie, organize e acompanhe tarefas com prioridade, recorrência e subtarefas"),
        Feature(Icons.Default.CalendarMonth,  "Calendário Inteligente", "Visualize tarefas por dia com feriados brasileiros integrados"),
        Feature(Icons.Default.Notifications,  "Lembretes Flexíveis",    "Receba até 12 lembretes por dia em horários estratégicos"),
        Feature(Icons.Default.PushPin,        "Tarefas Fixadas",        "Fixe as mais importantes no topo da lista"),
        Feature(Icons.Default.Group,          "Tarefas em Grupo",       "Colabore com outras pessoas em tarefas compartilhadas"),
        Feature(Icons.Default.CloudUpload,    "Backup na Nuvem",        "Salve seus dados no Google Drive com agendamento automático"),
        Feature(Icons.Default.Palette,        "Temas Personalizados",   "Modo claro/escuro com paleta warm brown exclusiva"),
        Feature(Icons.Default.BarChart,       "Resumo Diário",          "Acompanhe seu progresso e tarefas atrasadas em tempo real"),
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Tudo que o Cronos oferece",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(6.dp))
        Text("Seu assistente completo de produtividade",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))

        features.forEach { feat ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(feat.icon, null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(feat.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface)
                    Text(feat.desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ── Page 5: Plans ─────────────────────────────────────────

@Composable
private fun PagePlans(onVerPlanos: () -> Unit) {
    val plans = listOf(
        Triple("Basics", "R$ 8,00/mês", MaterialTheme.colorScheme.primary),
        Triple("Waves",  "R$ 6,50/mês", TypeSaude),
        Triple("Storm",  "R$ 4,99/mês", TypeEstudo)
    )
    val features = listOf(
        "Backup ilimitado na nuvem",
        "Seleção de horário para salvamento",
        "Backup automático agendado",
        "Tarefas em grupo"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Expanda suas possibilidades",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground)
        Spacer(Modifier.height(6.dp))
        Text("Escolha o plano ideal para você",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center)
        Spacer(Modifier.height(20.dp))

        plans.forEach { (name, price, color) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = color.copy(alpha = 0.1f)
                ),
                border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = color)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(color.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(price,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = color)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    features.forEach { feat ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 2.dp)
                        ) {
                            Icon(Icons.Default.Check, null,
                                modifier = Modifier.size(14.dp),
                                tint = color)
                            Spacer(Modifier.width(6.dp))
                            Text(feat,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        // CTA
        Button(
            onClick = onVerPlanos,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Default.Star, null, Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Ver todos os planos", fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(4.dp))

        Text("Ou continue gratuitamente — toque em Começar",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center)
    }
}
