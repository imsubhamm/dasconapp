package com.dascon.machinerylog.util

import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val ist: ZoneId = ZoneId.of("Asia/Kolkata")
private val ymd: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

fun todayYmdIst(): String = LocalDate.now(ist).format(ymd)

fun parseYmd(value: String): LocalDate = LocalDate.parse(value, ymd)

fun formatYmd(date: LocalDate): String = date.format(ymd)

fun displayDate(ymdStr: String): String {
    return runCatching {
        val d = parseYmd(ymdStr)
        d.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
    }.getOrDefault(ymdStr)
}
