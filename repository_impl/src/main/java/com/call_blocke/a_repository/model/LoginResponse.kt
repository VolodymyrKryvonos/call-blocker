package com.call_blocke.a_repository.model

data class LoginResponse(
    val success: TokenModel
)

data class TokenModel(
    val token: String
)
