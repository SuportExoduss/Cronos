package com.exoduss.cronos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exoduss.cronos.data.repository.PreferencesRepository
import com.exoduss.cronos.notifications.ReminderScheduler
import com.exoduss.cronos.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val reminderScheduler: ReminderScheduler
) : ViewModel() {

    val startDestination: StateFlow<String?> = combine(
        preferencesRepository.isPermissionsRequested,
        preferencesRepository.isOnboardingDone
    ) { permsRequested, onboardingDone ->
        when {
            !permsRequested -> Screen.Permissions.route
            !onboardingDone -> Screen.Onboarding.route
            else            -> Screen.Home.route
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val darkMode: StateFlow<Boolean> = preferencesRepository.darkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val useSystemTheme: StateFlow<Boolean> = preferencesRepository.useSystemTheme
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    init {
        // Garante que o resumo diário esteja sempre agendado enquanto o app estiver ativo
        viewModelScope.launch {
            val enabled = preferencesRepository.dailySummaryEnabled.first()
            val hour    = preferencesRepository.dailySummaryHour.first()
            if (enabled) reminderScheduler.scheduleDailySummary(hour)
        }
    }
}
