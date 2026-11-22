package com.gastoclaro.data.detection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
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
class SmsTransactionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var settingsRepository: SettingsRepository

    @Inject
    lateinit var processDetectedMessage: ProcessDetectedMessageUseCase

    @Inject
    lateinit var detectedTransactionHandler: DetectedTransactionHandler

    @Inject
    @ApplicationScope
    lateinit var appScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        val sender = messages.firstOrNull()?.originatingAddress ?: return
        val text = messages.joinToString("\n") { it.messageBody ?: "" }
        if (text.isBlank()) return

        val pendingResult = goAsync()
        appScope.launch {
            try {
                val senders = settingsRepository.monitoredSmsSenders.first()
                val matches = senders.any { sender.contains(it, ignoreCase = true) }
                if (!matches) return@launch

                val raw = RawMessage(
                    source = TransactionSource.SMS,
                    sender = sender,
                    text = text,
                    receivedAt = System.currentTimeMillis()
                )
                val result = processDetectedMessage(raw)
                if (result is DetectionResult.Detected) {
                    detectedTransactionHandler.handle(result.transaction)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
