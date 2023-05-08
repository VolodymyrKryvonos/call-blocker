package com.call_blocker.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class TaskStatusData(
    @PrimaryKey
    val id: Int,
    val status: String,
    val simId: String,
    val time: Long,
    val simIccid: String
)
