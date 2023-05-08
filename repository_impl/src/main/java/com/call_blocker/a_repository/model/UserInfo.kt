package com.call_blocker.a_repository.model

import com.squareup.moshi.Json

data class UserInfo(
    val user: UserModel
)

data class UserModel(
    val details: UserDetailInfo,
    val calculation: Float,
    val name: String?,
    @Json(name = "last_name")
    val lastName: String?
)
