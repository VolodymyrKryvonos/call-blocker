package com.call_blocke.app.screen.settings

import java.util.regex.Matcher
import java.util.regex.Pattern

private const val IPV4_PATTERN =
    "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$"
private val pattern: Pattern = Pattern.compile(IPV4_PATTERN)

fun isIpValid(ip: String?): Boolean {
    val matcher: Matcher = pattern.matcher(ip)
    return matcher.matches()
}
