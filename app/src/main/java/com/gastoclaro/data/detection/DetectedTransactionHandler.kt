package com.gastoclaro.data.detection

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.content.ContextCompat
import com.gastoclaro.core.notification.AppNotifications
import com.gastoclaro.domain.model.BubbleMode
import com.gastoclaro.domain.model.Transaction
import com.gastoclaro.domain.repository.SettingsRepository
import com.gastoclaro.domain.usecase.EnqueueExpenseQuestionUseCase
import com.gastoclaro.ui.bubble.BubbleService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DetectedTransactionHandler @Inject constructor(
    private val appNotifications: AppNotifications,
    private val enqueueExpenseQuestion: EnqueueExpenseQuestionUseCase,
    private val settingsRepository: SettingsRepository,
    @ApplicationContext private val context: Context
) {

    suspend fun handle(transaction: Transaction) {
        appNotifications.showDetected(transaction)
        val shouldAsk = enqueueExpenseQuestion(transaction)
        if (shouldAsk) launchBubbleIfAllowed()
    }

    private suspend fun launchBubbleIfAllowed() {
        val mode = settingsRepository.bubbleMode.first()
        if (mode == BubbleMode.OVERLAY && Settings.canDrawOverlays(context)) {
            val intent = Intent(context, BubbleService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }
    }
}
