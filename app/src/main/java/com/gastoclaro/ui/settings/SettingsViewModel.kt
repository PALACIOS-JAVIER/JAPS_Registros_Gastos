package com.gastoclaro.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gastoclaro.domain.model.BubbleMode
import com.gastoclaro.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val monitoredPackages: StateFlow<Set<String>> = settingsRepository.monitoredPackages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val smsSenders: StateFlow<Set<String>> = settingsRepository.monitoredSmsSenders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val matchWindow: StateFlow<Int> = settingsRepository.matchWindowMinutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10)

    val bubbleEnabled: StateFlow<Boolean> = settingsRepository.bubbleEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val bubbleMode: StateFlow<BubbleMode> = settingsRepository.bubbleMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BubbleMode.OVERLAY)

    val bubbleMinAmount: StateFlow<Long> = settingsRepository.bubbleMinAmount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val dndStart: StateFlow<String> = settingsRepository.dndStart
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "22:00")

    val dndEnd: StateFlow<String> = settingsRepository.dndEnd
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "07:00")

    val collapseSeconds: StateFlow<Int> = settingsRepository.bubbleAutoCollapseSeconds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 120)

    val neverAsk: StateFlow<Set<String>> = settingsRepository.neverAskCounterparties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptySet())

    val biometricEnabled: StateFlow<Boolean> = settingsRepository.biometricEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val flagSecureEnabled: StateFlow<Boolean> = settingsRepository.flagSecureEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val onboardingCompleted: StateFlow<Boolean> = settingsRepository.onboardingCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setMonitoredPackages(value: Set<String>) = launch { settingsRepository.setMonitoredPackages(value) }
    fun setSmsSenders(value: Set<String>) = launch { settingsRepository.setMonitoredSmsSenders(value) }
    fun setMatchWindow(value: Int) = launch { settingsRepository.setMatchWindowMinutes(value) }
    fun setBubbleEnabled(value: Boolean) = launch { settingsRepository.setBubbleEnabled(value) }
    fun setBubbleMode(value: BubbleMode) = launch { settingsRepository.setBubbleMode(value) }
    fun setBubbleMinAmount(value: Long) = launch { settingsRepository.setBubbleMinAmount(value) }
    fun setDndWindow(start: String, end: String) = launch { settingsRepository.setDndWindow(start, end) }
    fun setCollapseSeconds(value: Int) = launch { settingsRepository.setBubbleAutoCollapseSeconds(value) }
    fun setNeverAsk(value: Set<String>) = launch { settingsRepository.setNeverAskCounterparties(value) }
    fun setBiometricEnabled(value: Boolean) = launch { settingsRepository.setBiometricEnabled(value) }
    fun setFlagSecureEnabled(value: Boolean) = launch { settingsRepository.setFlagSecureEnabled(value) }
    fun setOnboardingCompleted(value: Boolean) = launch { settingsRepository.setOnboardingCompleted(value) }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
