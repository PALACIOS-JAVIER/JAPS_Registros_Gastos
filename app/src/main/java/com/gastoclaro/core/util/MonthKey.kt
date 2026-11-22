package com.gastoclaro.core.util

import java.time.Instant
import java.time.ZoneId

object MonthKey {
    fun of(dateTime: Long, zone: ZoneId = ZoneId.systemDefault()): String {
        val date = Instant.ofEpochMilli(dateTime).atZone(zone).toLocalDate()
        return "%04d-%02d".format(date.year, date.monthValue)
    }
}
