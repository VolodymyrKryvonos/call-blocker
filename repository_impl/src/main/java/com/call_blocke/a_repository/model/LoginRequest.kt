package com.call_blocke.a_repository.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("unique_id")
    override val uniqueId: String,
    override val email: String,
    override val password: String,
    @SerializedName("version_of_package")
    val version: String
): AuthRequest
