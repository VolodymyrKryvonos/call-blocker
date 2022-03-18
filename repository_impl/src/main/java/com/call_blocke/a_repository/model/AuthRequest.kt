package com.call_blocke.a_repository.model

import com.google.gson.annotations.SerializedName

interface AuthRequest {
    @get:SerializedName("unique_id")
    val uniqueId: String
    val email: String
    val password: String
}