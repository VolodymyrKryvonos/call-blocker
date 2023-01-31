package com.call_blocke.a_repository.model

import com.squareup.moshi.Json

interface AuthRequest {
    @get:Json(name = "unique_id")
    val uniqueId: String
    val email: String
    val password: String
}