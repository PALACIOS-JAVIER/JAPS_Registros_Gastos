package com.gastoclaro.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gastoclaro.domain.repository.SettingsRepository
import com.gastoclaro.ui.navigation.Destinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    settingsRepository: SettingsRepository
) : ViewModel() {

    val startDestination: StateFlow<String?> = settingsRepository.onboardingCompleted
        .map { completed ->
            if (completed) Destinations.HOME else Destinations.ONBOARDING
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
