package com.call_blocke.app.screen.sim_info

import java.util.regex.Matcher
import java.util.regex.Pattern


private const val PHONE_NUMBER_PATTERN =
    "\\d{8,15}"
private val pattern: Pattern = Pattern.compile(PHONE_NUMBER_PATTERN)


fun isPhoneValid(phone: String): Boolean{
    val matcher: Matcher = pattern.matcher(phone)
    return matcher.matches()
}