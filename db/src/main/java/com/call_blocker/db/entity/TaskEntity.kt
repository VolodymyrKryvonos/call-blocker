package com.call_blocker.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.call_blocker.db.TaskMethod
import java.util.*

@Entity(tableName = "task")
data class TaskEntity(
    @PrimaryKey
    val id: Int,

    val method: TaskMethod,

    val sendTo: String,

    val message: String,

    val highPriority: Boolean = false,

    var status: TaskStatus = TaskStatus.BUFFERED,

    val bufferedAt: Long = Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis,

    var processAt: Long = 0,

    var deliveredAt: Long = 0,

    var confirmAt: Long = 0,

    var simSlot: Int? = 0,

    val simIccId: String
)

enum class TaskStatus {
    BUFFERED,
    PROCESS,
    DELIVERED,
    TIME_RANGE_VIOLATED,
    ERROR
}