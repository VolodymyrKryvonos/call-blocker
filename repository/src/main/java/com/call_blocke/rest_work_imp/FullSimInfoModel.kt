package com.call_blocke.rest_work_imp

data class FullSimInfoModel(
    val simDate: String,
    val simDelivered: Int,
    val simPerDay: Int,
    val simSlot: Int
)