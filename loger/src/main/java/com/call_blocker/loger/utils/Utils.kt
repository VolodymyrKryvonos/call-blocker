package com.call_blocker.loger.utils

import java.io.PrintWriter
import java.io.StringWriter

fun getStackTrace(t: Throwable): String{
    val sw = StringWriter()
    val pw = PrintWriter(sw)
    t.printStackTrace(pw)
    return sw.toString()
}