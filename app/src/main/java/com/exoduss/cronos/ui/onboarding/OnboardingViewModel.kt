package com.exoduss.cronos.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.exoduss.cronos.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    fun completeOnboarding(
        userName: String,
        darkMode: Boolean,
        favoriteTypeId: String?,
        googleConnected: Boolean = false
    ) {
        viewModelScope.launch {
            if (userName.isNotBlank()) preferencesRepository.setUserName(userName.trim())
            preferencesRepository.setUseSystemTheme(false)
            preferencesRepository.setDarkMode(darkMode)
            if (favoriteTypeId != null) preferencesRepository.setFavoriteTypeId(favoriteTypeId)
            preferencesRepository.setGoogleConnected(googleConnected)
            preferencesRepository.setOnboardingDone()
        }
    }
}
