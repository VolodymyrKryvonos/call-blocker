package com.call_blocke.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "replay_task")
data class ReplayTaskEntity(
    @PrimaryKey
    val id: Int,

    val rInMsisdn: String,

    val rTextReply: String,

    val tText: String,

    val rOutMsisdn: String
)