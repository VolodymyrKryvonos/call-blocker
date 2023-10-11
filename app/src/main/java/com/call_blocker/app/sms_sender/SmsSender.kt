package com.call_blocker.app.sms_sender

import com.call_blocker.db.entity.TaskEntity

interface SmsSender {
    suspend fun doTask(task: TaskEntity): Boolean
}