package com.gastoclaro.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.gastoclaro.core.util.BankCatalog
import com.gastoclaro.domain.model.BubbleMode
import com.gastoclaro.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    private object Keys {
        val MONITORED_PACKAGES = stringSetPreferencesKey("monitored_packages")
        val SMS_SENDERS = stringSetPreferencesKey("sms_senders")
        val MATCH_WINDOW = intPreferencesKey("match_window_minutes")
        val BUBBLE_ENABLED = booleanPreferencesKey("bubble_enabled")
        val BUBBLE_MODE = stringPreferencesKey("bubble_mode")
        val BUBBLE_MIN_AMOUNT = longPreferencesKey("bubble_min_amount")
        val DND_START = stringPreferencesKey("dnd_start")
        val DND_END = stringPreferencesKey("dnd_end")
        val BUBBLE_COLLAPSE = intPreferencesKey("bubble_collapse_seconds")
        val NEVER_ASK = stringSetPreferencesKey("never_ask_counterparties")
        val BIOMETRIC = booleanPreferencesKey("biometric_enabled")
        val FLAG_SECURE = booleanPreferencesKey("flag_secure_enabled")
        val ONBOARDING = booleanPreferencesKey("onboarding_completed")
        val BUBBLE_X = intPreferencesKey("bubble_last_x")
        val BUBBLE_Y = intPreferencesKey("bubble_last_y")
    }

    override val monitoredPackages: Flow<Set<String>> =
        dataStore.data.map { it[Keys.MONITORED_PACKAGES] ?: BankCatalog.defaultPackages }

    override suspend fun setMonitoredPackages(packages: Set<String>) {
        dataStore.edit { it[Keys.MONITORED_PACKAGES] = packages }
    }

    override val monitoredSmsSenders: Flow<Set<String>> =
        dataStore.data.map { it[Keys.SMS_SENDERS] ?: BankCatalog.defaultSmsSenders }

    override suspend fun setMonitoredSmsSenders(senders: Set<String>) {
        dataStore.edit { it[Keys.SMS_SENDERS] = senders }
    }

    override val matchWindowMinutes: Flow<Int> =
        dataStore.data.map { it[Keys.MATCH_WINDOW] ?: 10 }

    override suspend fun setMatchWindowMinutes(minutes: Int) {
        dataStore.edit { it[Keys.MATCH_WINDOW] = minutes }
    }

    override val bubbleEnabled: Flow<Boolean> =
        dataStore.data.map { it[Keys.BUBBLE_ENABLED] ?: true }

    override suspend fun setBubbleEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.BUBBLE_ENABLED] = enabled }
    }

    override val bubbleMode: Flow<BubbleMode> =
        dataStore.data.map { prefs ->
            prefs[Keys.BUBBLE_MODE]?.let { mode -> runCatching { BubbleMode.valueOf(mode) }.getOrNull() }
                ?: BubbleMode.OVERLAY
        }

    override suspend fun setBubbleMode(mode: BubbleMode) {
        dataStore.edit { it[Keys.BUBBLE_MODE] = mode.name }
    }

    override val bubbleMinAmount: Flow<Long> =
        dataStore.data.map { it[Keys.BUBBLE_MIN_AMOUNT] ?: 0L }

    override suspend fun setBubbleMinAmount(amount: Long) {
        dataStore.edit { it[Keys.BUBBLE_MIN_AMOUNT] = amount }
    }

    override val dndStart: Flow<String> = dataStore.data.map { it[Keys.DND_START] ?: "22:00" }
    override val dndEnd: Flow<String> = dataStore.data.map { it[Keys.DND_END] ?: "07:00" }

    override suspend fun setDndWindow(start: String, end: String) {
        dataStore.edit {
            it[Keys.DND_START] = start
            it[Keys.DND_END] = end
        }
    }

    override val bubbleAutoCollapseSeconds: Flow<Int> =
        dataStore.data.map { it[Keys.BUBBLE_COLLAPSE] ?: 120 }

    override suspend fun setBubbleAutoCollapseSeconds(seconds: Int) {
        dataStore.edit { it[Keys.BUBBLE_COLLAPSE] = seconds }
    }

    override val neverAskCounterparties: Flow<Set<String>> =
        dataStore.data.map { it[Keys.NEVER_ASK] ?: emptySet() }

    override suspend fun setNeverAskCounterparties(values: Set<String>) {
        dataStore.edit { it[Keys.NEVER_ASK] = values }
    }

    override val biometricEnabled: Flow<Boolean> =
        dataStore.data.map { it[Keys.BIOMETRIC] ?: false }

    override suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.BIOMETRIC] = enabled }
    }

    override val flagSecureEnabled: Flow<Boolean> =
        dataStore.data.map { it[Keys.FLAG_SECURE] ?: true }

    override suspend fun setFlagSecureEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.FLAG_SECURE] = enabled }
    }

    override val onboardingCompleted: Flow<Boolean> =
        dataStore.data.map { it[Keys.ONBOARDING] ?: false }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { it[Keys.ONBOARDING] = completed }
    }

    override val bubbleLastX: Flow<Int> = dataStore.data.map { it[Keys.BUBBLE_X] ?: -1 }
    override val bubbleLastY: Flow<Int> = dataStore.data.map { it[Keys.BUBBLE_Y] ?: -1 }

    override suspend fun setBubblePosition(x: Int, y: Int) {
        dataStore.edit {
            it[Keys.BUBBLE_X] = x
            it[Keys.BUBBLE_Y] = y
        }
    }
}
