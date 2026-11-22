package com.gastoclaro.core.util

import com.gastoclaro.domain.model.Transaction
import com.gastoclaro.domain.model.TransactionType

object CsvExporter {

    fun build(
        transactions: List<Transaction>,
        accountNames: Map<Long, String>,
        categoryNames: Map<Long, String>
    ): String {
        val sb = StringBuilder()
        sb.append("fecha,tipo,monto,comercio,categoria,cuenta,nota\n")
        transactions.forEach { tx ->
            val fecha = DateFormats.shortDate(tx.dateTime) + " " + DateFormats.time(tx.dateTime)
            val tipo = typeLabel(tx.type, tx.isTransferInternal)
            val monto = MoneyFormatter.format(tx.amount)
            val comercio = (tx.merchant ?: "").replace(",", " ")
            val categoria = tx.categoryId?.let { categoryNames[it] ?: "" }.orEmpty().replace(",", " ")
            val cuenta = (accountNames[tx.accountId] ?: "").replace(",", " ")
            val nota = (tx.note ?: "").replace(",", " ").replace("\n", " ")
            sb.append("\"$fecha\",\"$tipo\",$monto,\"$comercio\",\"$categoria\",\"$cuenta\",\"$nota\"\n")
        }
        return sb.toString()
    }

    private fun typeLabel(type: TransactionType, isInternal: Boolean): String =
        if (isInternal) "Transferencia propia" else when (type) {
            TransactionType.EXPENSE -> "Gasto"
            TransactionType.INCOME -> "Ingreso"
            TransactionType.TRANSFER_OUT -> "Transferencia enviada"
            TransactionType.TRANSFER_IN -> "Transferencia recibida"
            TransactionType.WITHDRAWAL -> "Retiro"
        }
}
