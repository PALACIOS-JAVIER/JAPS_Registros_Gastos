package com.gastoclaro.core.util

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object DateTimeParser {

    private val timeRegex = Regex("""(\d{1,2}):(\d{2})(?::(\d{2}))?\s*([aApP]\.?[mM]\.?)?""")
    private val isoDate = Regex("""\b(\d{4})[/\-.](\d{1,2})[/\-.](\d{1,2})\b""")
    private val dmyDate = Regex("""\b(\d{1,2})[/\-.](\d{1,2})[/\-.](\d{2,4})\b""")
    private val monthNameRegex = Regex(
        """\b(\d{1,2})\s+(?:de\s+)?([a-zA-ZáéíóúñÁÉÍÓÚÑ]{3,9})(?:\s+(?:de\s+|del\s+)?(\d{2,4}))?"""
    )

    fun parse(text: String, receivedAt: Long, now: Long = System.currentTimeMillis()): Long {
        val zone = ZoneId.systemDefault()
        val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()

        var date: LocalDate? = null
        var time: LocalTime? = null

        if (Regex("(?i)\\bhoy\\b").containsMatchIn(text)) {
            date = today
        } else if (Regex("(?i)\\bayer\\b").containsMatchIn(text)) {
            date = today.minusDays(1)
        }

        timeRegex.find(text)?.let { m ->
            var hour = m.groupValues[1].toInt()
            val minute = m.groupValues[2].toInt()
            val second = m.groupValues[3].ifEmpty { "0" }.toInt()
            val meridiem = m.groupValues[4].lowercase()
            if (meridiem.startsWith("p") && hour < 12) hour += 12
            if (meridiem.startsWith("a") && hour == 12) hour = 0
            time = try { LocalTime.of(hour, minute, second) } catch (e: Exception) { null }
        }

        if (date == null) {
            isoDate.find(text)?.let { m ->
                date = try {
                    LocalDate.of(m.groupValues[1].toInt(), m.groupValues[2].toInt(), m.groupValues[3].toInt())
                } catch (e: Exception) { null }
            }
        }

        if (date == null) {
            dmyDate.find(text)?.let { m ->
                val day = m.groupValues[1].toInt()
                val month = m.groupValues[2].toInt()
                var year = m.groupValues[3].toInt()
                if (year < 100) year += 2000
                date = try { LocalDate.of(year, month, day) } catch (e: Exception) { null }
            }
        }

        if (date == null) {
            monthNameRegex.find(text)?.let { m ->
                val day = m.groupValues[1].toInt()
                val month = monthNumber(m.groupValues[2]) ?: return@let
                val yearStr = m.groupValues[3]
                val year = when {
                    yearStr.isBlank() -> today.year
                    yearStr.length <= 2 -> 2000 + yearStr.toInt()
                    else -> yearStr.toInt()
                }
                date = try { LocalDate.of(year, month, day) } catch (e: Exception) { null }
            }
        }

        if (date == null && time == null) return receivedAt

        val effectiveDate = date ?: today
        val effectiveTime = time ?: LocalTime.of(0, 0)
        return effectiveDate.atTime(effectiveTime).atZone(zone).toInstant().toEpochMilli()
    }

    private fun monthNumber(name: String): Int? {
        val n = name.lowercase().trim()
        val map = mapOf(
            "enero" to 1, "ene" to 1, "jan" to 1,
            "febrero" to 2, "feb" to 2,
            "marzo" to 3, "mar" to 3,
            "abril" to 4, "abr" to 4, "apr" to 4,
            "mayo" to 5, "may" to 5,
            "junio" to 6, "jun" to 6,
            "julio" to 7, "jul" to 7,
            "agosto" to 8, "ago" to 8, "aug" to 8,
            "septiembre" to 9, "sept" to 9, "setiembre" to 9, "sep" to 9,
            "octubre" to 10, "oct" to 10,
            "noviembre" to 11, "nov" to 11,
            "diciembre" to 12, "dic" to 12, "dec" to 12
        )
        return map[n]
    }
}
