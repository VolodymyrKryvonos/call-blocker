package com.call_blocke.a_repository.model

import com.google.gson.annotations.SerializedName

data class UserDetailInfo(
    @SerializedName("left_count")
    val leftCount: Int,

    @SerializedName("delivered_count")
    val deliveredCount: Int,

    @SerializedName("undelivered_count")
    val undeliveredCount: Int
)
