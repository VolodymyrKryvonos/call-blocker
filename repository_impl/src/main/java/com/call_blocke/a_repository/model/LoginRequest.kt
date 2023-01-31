package com.call_blocke.a_repository.model

import com.squareup.moshi.Json

data class LoginRequest(
    @Json(name = "unique_id")
    override val uniqueId: String,
    override val email: String,
    override val password: String,
    @Json(name = "version_of_package")
    val version: String
): AuthRequest
