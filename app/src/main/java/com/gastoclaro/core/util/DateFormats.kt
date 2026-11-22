package com.gastoclaro.core.util

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

object DateFormats {

    private val monthNames = listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre"
    )

    fun shortDate(millis: Long): String {
        val d = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        return "%02d/%02d/%d".format(d.dayOfMonth, d.monthValue, d.year)
    }

    fun time(millis: Long): String {
        val t = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalTime()
        return "%02d:%02d".format(t.hour, t.minute)
    }

    fun dayMonth(millis: Long): String {
        val d = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
        return "%d %s".format(d.dayOfMonth, monthNames[d.monthValue - 1])
    }

    fun fullDate(millis: Long): String = "${dayMonth(millis)} ${year(millis)}"

    fun year(millis: Long): Int =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).year

    fun dayGroupLabel(millis: Long): String {
        val zone = ZoneId.systemDefault()
        val date = Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()
        val today = LocalDate.now(zone)
        return when (date) {
            today -> "Hoy"
            today.minusDays(1) -> "Ayer"
            else -> "%d de %s".format(date.dayOfMonth, monthNames[date.monthValue - 1])
        }
    }

    fun monthLabel(monthKey: String): String {
        val parts = monthKey.split("-")
        if (parts.size != 2) return monthKey
        val month = parts[1].toIntOrNull() ?: return monthKey
        if (month !in 1..12) return monthKey
        val short = monthNames[month - 1].take(3).replaceFirstChar { it.uppercase() }
        return "$short ${parts[0]}"
    }

    fun currentMonthKey(): String {
        val d = LocalDate.now(ZoneId.systemDefault())
        return "%04d-%02d".format(d.year, d.monthValue)
    }
}
