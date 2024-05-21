package com.call_blocker.app.sms_sender

import com.call_blocker.db.entity.TaskEntity
import com.call_blocker.rest_work_imp.TaskRepository
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

class TestSmsSender(
    private val taskRepository: TaskRepository
) : SmsSender {
    override suspend fun doTask(task: TaskEntity): Boolean {
        taskRepository.taskOnProcess(task, task.simSlot?: return false)
        delay(2.seconds)
        if (task.message != "undelivered"){
            taskRepository.taskOnDelivered(task)
        }else{
            taskRepository.taskOnError(task)
        }
        return false
    }
}