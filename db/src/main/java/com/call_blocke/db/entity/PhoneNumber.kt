package com.call_blocke.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PhoneNumber(
    @PrimaryKey
    val phoneNumber: String
)
