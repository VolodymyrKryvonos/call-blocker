package com.call_blocker.a_repository.model

import com.squareup.moshi.Json

data class LoginRequest(
    override val email: String,
    override val password: String,
    @Json(name = "version_of_package")
    val version: String
): AuthRequest
