package com.gastoclaro.data.detection

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.gastoclaro.core.di.ApplicationScope
import com.gastoclaro.domain.model.TransactionSource
import com.gastoclaro.domain.parser.RawMessage
import com.gastoclaro.domain.repository.SettingsRepository
import com.gastoclaro.domain.usecase.DetectionResult
import com.gastoclaro.domain.usecase.ProcessDetectedMessageUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TransactionNotificationListener : NotificationListenerService() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var processDetectedMessage: ProcessDetectedMessageUseCase

    @Inject
    lateinit var detectedTransactionHandler: DetectedTransactionHandler

    @Inject
    @ApplicationScope
    lateinit var appScope: CoroutineScope

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        val packageName = sbn?.packageName ?: return
        val text = extractText(sbn)
        if (text.isBlank()) return

        appScope.launch {
            val monitored = settingsRepository.monitoredPackages.first()
            if (packageName !in monitored) return@launch

            val raw = RawMessage(
                source = TransactionSource.NOTIFICATION,
                appPackage = packageName,
                text = text,
                receivedAt = System.currentTimeMillis()
            )
            val result = processDetectedMessage(raw)
            if (result is DetectionResult.Detected) {
                detectedTransactionHandler.handle(result.transaction)
            }
        }
    }

    private fun extractText(sbn: StatusBarNotification): String {
        val extras = sbn.notification?.extras ?: return ""
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()
        val subText = extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString().orEmpty()
        val lines = extras.getCharSequenceArray(Notification.EXTRA_TEXT_LINES)?.joinToString("\n").orEmpty()
        return listOf(title, text, bigText, subText, lines)
            .filter { it.isNotBlank() }
            .joinToString("\n")
            .trim()
    }
}
