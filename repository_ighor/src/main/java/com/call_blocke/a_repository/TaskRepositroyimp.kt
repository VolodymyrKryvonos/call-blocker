package com.call_blocke.a_repository

import com.call_blocke.a_repository.model.TaskStatusRequest
import com.call_blocke.a_repository.model.TasksRequest
import com.call_blocke.a_repository.rest.TaskRest
import com.call_blocke.db.entity.TaskEntity
import com.call_blocke.db.entity.TaskStatus
import com.call_blocke.rest_work_imp.TaskRepository

class TaskRepositoryImp: TaskRepository() {

    private val taskRest = ApiRepositoryHelper.createRest(TaskRest::class.java)

    override suspend fun loadTasks(): List<TaskEntity> {
        return taskRest.tasks(TasksRequest(
            campaign = "App SMS",
            connectionType = "WIFI",
            carrier = ""
        )).data.flatMap {
            it.smsList
        }.map {
            TaskEntity(
                id = it.id,
                sendTo = it.msisdn,
                message = it.txt
            )
        }
    }

    override suspend fun confirmTask(data: TaskEntity) {
        taskRest.confirmStatus(TaskStatusRequest(
            id = data.id,
            error = if (data.status == TaskStatus.DELIVERED)
                "SUCCESS"
            else "Generic failure error",
            statusCode = if (data.status == TaskStatus.DELIVERED) 1 else 0,
            simId = if (data.simSlot == 0)
                "msisdn_1"
            else "msisdn_2"
        ))
    }
}