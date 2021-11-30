package com.call_blocke.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "task")
data class TaskEntity(
    @PrimaryKey
    val id: Int,

    val sendTo: String,

    val message: String,

    var status: TaskStatus = TaskStatus.BUFFERED,

    val bufferedAt: Long = Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis,

    var processAt: Long = 0,

    var deliveredAt: Long = 0,

    var confirmAt: Long = 0,

    var simSlot: Int? = 0
)

enum class TaskStatus {
    BUFFERED,
    PROCESS,
    DELIVERED,
    ERROR
}