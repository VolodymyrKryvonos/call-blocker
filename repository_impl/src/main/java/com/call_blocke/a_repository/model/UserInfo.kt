package com.call_blocke.a_repository.model

import com.google.gson.annotations.SerializedName

data class UserInfo(
    val user: UserModel
)

data class UserModel(
    val details: UserDetailInfo,
    val calculation: Float,
    val name: String?,
    @SerializedName("last_name")
    val lastName: String?
)
