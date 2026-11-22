package com.gastoclaro.core.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.gastoclaro.MainActivity
import com.gastoclaro.R
import com.gastoclaro.core.util.MoneyFormatter
import com.gastoclaro.data.detection.TransactionActionReceiver
import com.gastoclaro.domain.model.Transaction
import com.gastoclaro.domain.model.TransactionType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppNotifications @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        const val CHANNEL_DETECTED = "detected_transactions"
        const val CHANNEL_BUBBLE = "bubble_service"
        const val CHANNEL_REMINDER = "review_reminders"
        const val CHANNEL_BACKUP = "backup"
    }

    fun createChannels() {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_DETECTED, "Movimientos detectados", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Avisos de gastos e ingresos detectados automáticamente"
                setSound(null, null)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_BUBBLE, "Burbuja de preguntas", NotificationManager.IMPORTANCE_LOW).apply {
                description = "Servicio de la burbuja flotante"
                setSound(null, null)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_REMINDER, "Recordatorios", NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = "Movimientos por revisar"
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_BACKUP, "Respaldo", NotificationManager.IMPORTANCE_DEFAULT)
        )
    }

    private fun canNotify(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return false
        }
        return NotificationManagerCompat.from(context).areNotificationsEnabled()
    }

    fun showDetected(transaction: Transaction) {
        if (!canNotify()) return
        val id = (1000 + transaction.id).toInt()
        val amount = MoneyFormatter.formatShort(transaction.amount)
        val label = transaction.merchant ?: ""
        val title = when (transaction.type) {
            TransactionType.EXPENSE -> "Gasto detectado: $amount" + if (label.isNotBlank()) " en $label" else ""
            TransactionType.INCOME -> "Ingreso detectado: $amount"
            TransactionType.TRANSFER_IN -> "Transferencia recibida: $amount"
            TransactionType.TRANSFER_OUT -> "Transferencia enviada: $amount"
            TransactionType.WITHDRAWAL -> "Retiro detectado: $amount"
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            id,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(NotificationActions.EXTRA_TRANSACTION_ID, transaction.id)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val markInternalIntent = PendingIntent.getBroadcast(
            context,
            id + 1,
            Intent(context, TransactionActionReceiver::class.java).apply {
                action = NotificationActions.ACTION_MARK_INTERNAL
                putExtra(NotificationActions.EXTRA_TRANSACTION_ID, transaction.id)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val changeCategoryIntent = PendingIntent.getActivity(
            context,
            id + 2,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(NotificationActions.EXTRA_TRANSACTION_ID, transaction.id)
                putExtra(NotificationActions.EXTRA_OPEN_CATEGORIZATION, true)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_DETECTED)
            .setSmallIcon(R.drawable.ic_stat_gastoclaro)
            .setContentTitle(title)
            .setContentText(if (label.isNotBlank()) "Detectado automáticamente" else "Toca para revisar")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    when (transaction.type) {
                        TransactionType.EXPENSE -> "Se registró un gasto de $amount en $label"
                        TransactionType.INCOME -> "Se registró un ingreso de $amount"
                        TransactionType.TRANSFER_IN -> "Recibiste $amount"
                        TransactionType.TRANSFER_OUT -> "Enviaste $amount a $label"
                        TransactionType.WITHDRAWAL -> "Retiraste $amount"
                    }
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .addAction(0, "Cambiar categoría", changeCategoryIntent)
            .addAction(0, "Es transferencia mía", markInternalIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id, notification)
        } catch (e: SecurityException) {
            // Permission revoked between check and notify
        }
    }
}
