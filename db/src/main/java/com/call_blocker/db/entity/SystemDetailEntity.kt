package com.call_blocker.db.entity

data class SystemDetailEntity(
    val leftCount: Int = 0,
    val deliveredCount: Int = 0,
    val undeliveredCount: Int = 0,
    val amount: Float = 0f,
    val firstName: String = "",
    val lastName: String = ""
)