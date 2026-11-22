package com.gastoclaro.domain.parser

import com.gastoclaro.core.util.DateTimeParser
import com.gastoclaro.core.util.MoneyParser
import com.gastoclaro.core.util.TextNormalizer
import com.gastoclaro.domain.model.TransactionType

object ParseSupport {

    val transactionKeywords = Regex(
        """(?i)\b(compra|pago|pagaste|transferencia|retiro|retiraste|enviaste|envio|enviaste a|llegaron|llego|giro|deposito|recibiste|abono|consignacion|cobro|debito|debitado)\b"""
    )

    fun amount(text: String): Long? = MoneyParser.parseColombianAmount(text)

    fun dateTime(text: String, receivedAt: Long): Long = DateTimeParser.parse(text, receivedAt)

    fun detectType(text: String): TransactionType? {
        val t = TextNormalizer.normalize(text)
        return when {
            Regex("""\b(retiro|retiraste|retiro en cajero|retiro de cajero|retiro en el cajero)\b""").containsMatchIn(t) ->
                TransactionType.WITHDRAWAL
            Regex("""\b(deposito|consignacion|abono|nomina|salario|pago recibido|pago de nomina)\b""").containsMatchIn(t) ->
                TransactionType.INCOME
            Regex("""\b(transferencia enviada|enviaste|envio|enviaste a|transferiste|transferi|transferencia a|enviaste un|envio un)\b""").containsMatchIn(t) ->
                TransactionType.TRANSFER_OUT
            Regex("""\b(transferencia recibida|recibiste|recibio|te llego|te llegaron|llego un giro|giro recibido|te llego un giro|recibiste un|recibio un|te transfirieron|recibiste una)\b""").containsMatchIn(t) ->
                TransactionType.TRANSFER_IN
            Regex("""\b(compra|pago|pagaste|pago de servicios|cobro|debito|debitado|compra a credito|compra a cuotas|compra por)\b""").containsMatchIn(t) ->
                TransactionType.EXPENSE
            else -> null
        }
    }

    fun last4(text: String): String? {
        val patterns = listOf(
            Regex("""\*(\d{3,4})"""),
            Regex("""(?i)(?:cuenta|tarjeta)\s*(?:terminada en|finalizada en|que termina en)?\s*\**(\d{3,4})"""),
            Regex("""(?i)(?:cuenta|tarjeta)\s*\.{2,}\s*(\d{3,4})"""),
            Regex("""\b(\d{4})\b\s*$""")
        )
        for (p in patterns) {
            p.find(text)?.let { return it.groupValues[1].takeLast(4) }
        }
        return null
    }

    fun reference(text: String): String? {
        val patterns = listOf(
            Regex("""(?i)\bref(?:erencia)?\s*[:#]?\s*(\d+)"""),
            Regex("""(?i)\baprobacion\s*[:#]?\s*(\d+)"""),
            Regex("""(?i)\btransaccion\s*[:#]?\s*(\d+)"""),
            Regex("""(?i)\bnro?\.?\s*de\s*(?:transaccion|operacion)\s*[:#]?\s*(\d+)""")
        )
        for (p in patterns) {
            p.find(text)?.let { return it.groupValues[1] }
        }
        return null
    }

    fun balance(text: String): Long? {
        val m = Regex("""(?i)(?:saldo|disponible|saldo disponible|saldo actual)\s*:?\s*(\$?[\d.,]+)""")
            .find(text) ?: return null
        return MoneyParser.parseAmountToken(m.groupValues[1])
    }

    fun grab(text: String, pattern: Regex, group: Int = 1): String? =
        pattern.find(text)?.groupValues?.get(group)?.trim()?.takeIf { it.isNotEmpty() }

    fun extractMerchant(text: String, type: TransactionType): String? = when (type) {
        TransactionType.EXPENSE -> expenseMerchant(text)
        TransactionType.TRANSFER_OUT -> grab(
            text,
            Regex("""(?i)\ba\s+([A-ZÁÉÍÓÚÑ][A-ZÁÉÍÓÚÑ0-9 .&'-]*?)(?:\s+desde|\s+de\s+(?:la\s+)?cuenta|\s*\.|$)""")
        )
        TransactionType.TRANSFER_IN -> grab(
            text,
            Regex("""(?i)\bde\s+([A-ZÁÉÍÓÚÑ][A-ZÁÉÍÓÚÑ0-9 .&'-]*?)(?:\s+a\s+(?:la\s+)?cuenta|\s*\.|$)""")
        )
        TransactionType.WITHDRAWAL -> withdrawalMerchant(text)
        else -> null
    }

    private fun expenseMerchant(text: String): String? {
        grab(
            text,
            Regex("""(?i)\ben\s+([A-ZÁÉÍÓÚÑ0-9][A-ZÁÉÍÓÚÑ0-9 .&'-]*?)(?:\s+por|\s+con|\s*\.|$)""")
        )?.let { return it }
        grab(
            text,
            Regex("""(?i)\ba\s+(?!credito\b|cuotas\b)([A-ZÁÉÍÓÚÑ0-9][A-ZÁÉÍÓÚÑ0-9 .&'-]*?)(?:\s+por|\s+con|\s*\.|$)""")
        )?.let { return it }
        return null
    }

    private fun withdrawalMerchant(text: String): String? {
        val cajero = Regex("""(?i)\bcajero\b""").find(text) ?: return null
        val after = text.substring(cajero.range.last + 1).trimStart()
        val bank = Regex("""^([A-ZÁÉÍÓÚÑ][A-Za-zÁÉÍÓÚÑ]*)""").find(after)?.groupValues?.get(1)
        return if (bank.isNullOrEmpty()) "Cajero" else "Cajero $bank"
    }

    fun cleanMerchant(value: String?): String? = value
        ?.trim()
        ?.trimEnd('.', ',', ' ')
        ?.takeIf { it.isNotEmpty() }
        ?.let { it.replace(Regex("""\s*con\s+(?:tarjeta|cuenta).*$""", RegexOption.IGNORE_CASE), "") }
        ?.trim()
}
