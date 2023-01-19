package com.call_blocke.a_repository.model

import com.squareup.moshi.Json

data class UserDetailInfo(
    @Json(name = "left_count")
    val leftCount: Int,

    @Json(name = "delivered_count")
    val deliveredCount: Int,

    @Json(name = "undelivered_count")
    val undeliveredCount: Int
)
