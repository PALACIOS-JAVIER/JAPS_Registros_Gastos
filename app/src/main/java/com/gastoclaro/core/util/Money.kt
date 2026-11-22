package com.gastoclaro.core.util

import kotlin.math.abs

object MoneyParser {

    private val withDotsAndDecimal = Regex("""\$?\s*(\d{1,3}(?:\.\d{3})+),(\d{1,2})""")
    private val withDots = Regex("""\$?\s*(\d{1,3}(?:\.\d{3})+)""")
    private val simpleDecimal = Regex("""\$?\s*(\d{1,9}),(\d{1,2})\b""")
    private val plain = Regex("""\$?\s*(\d{4,})\b""")

    fun parseColombianAmount(text: String): Long? {
        withDotsAndDecimal.find(text)?.let { m ->
            val integer = m.groupValues[1].replace(".", "").toLongOrNull() ?: return null
            val decimals = m.groupValues[2].padEnd(2, '0').take(2).toLongOrNull() ?: 0L
            return integer * 100 + decimals
        }
        withDots.find(text)?.let { m ->
            val integer = m.groupValues[1].replace(".", "").toLongOrNull() ?: return null
            return integer * 100
        }
        simpleDecimal.find(text)?.let { m ->
            val integer = m.groupValues[1].toLongOrNull() ?: return null
            val decimals = m.groupValues[2].padEnd(2, '0').take(2).toLongOrNull() ?: 0L
            return integer * 100 + decimals
        }
        plain.find(text)?.let { m ->
            return m.groupValues[1].toLongOrNull()?.times(100)
        }
        return null
    }

    fun parseAmountToken(token: String): Long? {
        val t = token.trim().removePrefix("$").trim()
        if (t.isEmpty()) return null
        val negative = t.startsWith("-")
        val body = t.removePrefix("-")
        val sign = if (negative) -1L else 1L
        return when {
            body.contains(".") && body.contains(",") -> {
                val integer = body.substringBefore(",").replace(".", "").toLongOrNull() ?: return null
                val decimals = body.substringAfter(",").padEnd(2, '0').take(2).toLongOrNull() ?: 0L
                sign * (integer * 100 + decimals)
            }
            body.contains(".") -> {
                val integer = body.replace(".", "").toLongOrNull() ?: return null
                sign * integer * 100
            }
            body.contains(",") -> {
                val integer = body.substringBefore(",").toLongOrNull() ?: return null
                val decimals = body.substringAfter(",").padEnd(2, '0').take(2).toLongOrNull() ?: 0L
                sign * (integer * 100 + decimals)
            }
            else -> {
                sign * (body.toLongOrNull() ?: return null) * 100
            }
        }
    }
}

object MoneyFormatter {
    fun format(centavos: Long): String {
        val negative = centavos < 0
        val value = abs(centavos)
        val integer = value / 100
        val decimals = (value % 100).toInt()
        val integerStr = integer.toString().reversed().chunked(3).joinToString(".").reversed()
        val result = if (decimals == 0) integerStr else "$integerStr,${decimals.toString().padStart(2, '0')}"
        return (if (negative) "-" else "") + result
    }

    fun formatShort(centavos: Long): String = "$${format(centavos)}"
}
