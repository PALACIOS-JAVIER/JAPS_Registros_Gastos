package com.gastoclaro.domain.repository

import com.gastoclaro.domain.model.BubbleMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val monitoredPackages: Flow<Set<String>>
    suspend fun setMonitoredPackages(packages: Set<String>)

    val monitoredSmsSenders: Flow<Set<String>>
    suspend fun setMonitoredSmsSenders(senders: Set<String>)

    val matchWindowMinutes: Flow<Int>
    suspend fun setMatchWindowMinutes(minutes: Int)

    val bubbleEnabled: Flow<Boolean>
    suspend fun setBubbleEnabled(enabled: Boolean)

    val bubbleMode: Flow<BubbleMode>
    suspend fun setBubbleMode(mode: BubbleMode)

    val bubbleMinAmount: Flow<Long>
    suspend fun setBubbleMinAmount(amount: Long)

    val dndStart: Flow<String>
    val dndEnd: Flow<String>
    suspend fun setDndWindow(start: String, end: String)

    val bubbleAutoCollapseSeconds: Flow<Int>
    suspend fun setBubbleAutoCollapseSeconds(seconds: Int)

    val neverAskCounterparties: Flow<Set<String>>
    suspend fun setNeverAskCounterparties(values: Set<String>)

    val biometricEnabled: Flow<Boolean>
    suspend fun setBiometricEnabled(enabled: Boolean)

    val flagSecureEnabled: Flow<Boolean>
    suspend fun setFlagSecureEnabled(enabled: Boolean)

    val onboardingCompleted: Flow<Boolean>
    suspend fun setOnboardingCompleted(completed: Boolean)

    val bubbleLastX: Flow<Int>
    val bubbleLastY: Flow<Int>
    suspend fun setBubblePosition(x: Int, y: Int)
}
