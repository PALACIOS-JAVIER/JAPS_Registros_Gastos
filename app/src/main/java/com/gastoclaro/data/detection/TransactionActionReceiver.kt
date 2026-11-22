package com.gastoclaro.data.detection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.gastoclaro.core.di.ApplicationScope
import com.gastoclaro.core.notification.NotificationActions
import com.gastoclaro.domain.usecase.MarkTransactionInternalUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TransactionActionReceiver : BroadcastReceiver() {

    @Inject
    lateinit var markTransactionInternal: MarkTransactionInternalUseCase

    @Inject
    @ApplicationScope
    lateinit var appScope: CoroutineScope

    override fun onReceive(context: Context, intent: Intent) {
        val transactionId = intent.getLongExtra(NotificationActions.EXTRA_TRANSACTION_ID, -1L)
        if (transactionId < 0) return

        when (intent.action) {
            NotificationActions.ACTION_MARK_INTERNAL -> appScope.launch {
                markTransactionInternal(transactionId)
            }
        }
    }
}
