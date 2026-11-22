package com.gastoclaro.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CallMade
import androidx.compose.material.icons.rounded.CallReceived
import androidx.compose.material.icons.rounded.LocalAtm
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.gastoclaro.domain.model.Transaction
import com.gastoclaro.domain.model.TransactionType
import com.gastoclaro.ui.theme.ExpenseCoral
import com.gastoclaro.ui.theme.IncomeGreen
import com.gastoclaro.ui.theme.TransferViolet

data class TransactionVisual(
    val color: Color,
    val icon: ImageVector,
    val signedAmount: Long,
    val label: String
)

fun visualFor(type: TransactionType, amount: Long, isInternal: Boolean): TransactionVisual = when {
    isInternal -> TransactionVisual(
        color = TransferViolet,
        icon = Icons.Rounded.SwapHoriz,
        signedAmount = 0L,
        label = "Transferencia propia"
    )
    type == TransactionType.EXPENSE || type == TransactionType.TRANSFER_OUT || type == TransactionType.WITHDRAWAL ->
        TransactionVisual(
            color = ExpenseCoral,
            icon = when (type) {
                TransactionType.TRANSFER_OUT -> Icons.Rounded.CallMade
                TransactionType.WITHDRAWAL -> Icons.Rounded.LocalAtm
                else -> Icons.Rounded.ArrowUpward
            },
            signedAmount = -amount,
            label = when (type) {
                TransactionType.TRANSFER_OUT -> "Transferencia enviada"
                TransactionType.WITHDRAWAL -> "Retiro"
                else -> "Gasto"
            }
        )
    else -> TransactionVisual(
        color = IncomeGreen,
        icon = when (type) {
            TransactionType.TRANSFER_IN -> Icons.Rounded.CallReceived
            else -> Icons.Rounded.ArrowDownward
        },
        signedAmount = amount,
        label = when (type) {
            TransactionType.TRANSFER_IN -> "Transferencia recibida"
            else -> "Ingreso"
        }
    )
}

fun visualFor(transaction: Transaction): TransactionVisual =
    visualFor(transaction.type, transaction.amount, transaction.isTransferInternal)
