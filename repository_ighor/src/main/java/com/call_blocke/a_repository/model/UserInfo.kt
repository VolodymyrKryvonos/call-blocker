package com.call_blocke.a_repository.model

data class UserInfo(
    val user: UserModel
)

data class UserModel(
    val details: UserDetailInfo,
    val calculation: Float
)
