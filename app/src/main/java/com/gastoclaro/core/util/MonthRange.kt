package com.gastoclaro.core.util

import java.time.LocalDate
import java.time.ZoneId

object MonthRange {

    fun current(): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val first = LocalDate.now(zone).withDayOfMonth(1)
        return rangeFor(first)
    }

    fun forMonthKey(monthKey: String): Pair<Long, Long> {
        val parts = monthKey.split("-")
        if (parts.size != 2) return current()
        val year = parts[0].toIntOrNull() ?: return current()
        val month = parts[1].toIntOrNull() ?: return current()
        val first = LocalDate.of(year, month, 1)
        return rangeFor(first)
    }

    fun monthKeys(count: Int): List<String> {
        val zone = ZoneId.systemDefault()
        val now = LocalDate.now(zone).withDayOfMonth(1)
        return (count - 1 downTo 0).map { offset ->
            val d = now.minusMonths(offset.toLong())
            "%04d-%02d".format(d.year, d.monthValue)
        }
    }

    private fun rangeFor(first: LocalDate): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val start = first.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = first.plusMonths(1).atStartOfDay(zone).toInstant().toEpochMilli()
        return start to end
    }
}
