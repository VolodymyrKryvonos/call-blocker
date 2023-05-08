package com.call_blocker.a_repository.model

import com.squareup.moshi.Json

interface AuthRequest {
    @Json(name = "unique_id")
    val uniqueId: String
    val email: String
    val password: String
}