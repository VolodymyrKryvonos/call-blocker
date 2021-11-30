package com.call_blocke.a_repository.repository

import android.util.Log
import com.call_blocke.a_repository.model.*
import com.call_blocke.a_repository.rest.TaskRest
import com.call_blocke.a_repository.socket.SocketBuilder
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.db.entity.TaskEntity
import com.call_blocke.db.entity.TaskStatus
import com.call_blocke.rest_work_imp.TaskMessage
import com.call_blocke.rest_work_imp.TaskRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.*

@DelicateCoroutinesApi
class TaskRepositoryImp: TaskRepository() {

    private val taskRest = ApiRepositoryHelper.createRest(TaskRest::class.java)

    private val socketBuilder by lazy {
        SocketBuilder
            .Builder
            .setUserToken(SmsBlockerDatabase.userToken ?: "jhfhjlbdhjlf")
            .setUUid(SmsBlockerDatabase.deviceID)
            .build()
    }

    override suspend fun loadTasks(): List<TaskEntity> {
        return taskRest.tasks(TasksRequest()).data.flatMap {
            it.smsList
        }.map {
            TaskEntity(
                id = it.id,
                sendTo = it.msisdn,
                message = it.txt
            )
        }
    }

    override suspend fun confirmTask(data: List<TaskEntity>) {
        taskRest.confirmStatus(ConfirmStatusRequest(
            data = data.map {
                TaskStatusRequest(
                    id = it.id,
                    error = if (it.status == TaskStatus.DELIVERED)
                        "SUCCESS"
                    else "Generic failure error",
                    statusCode = if (it.status == TaskStatus.DELIVERED) 1 else 0,
                    simId = if (it.simSlot == 0)
                        "msisdn_1"
                    else "msisdn_2"
                )
            }
        ))
    }

    override fun taskMessage(): Flow<TaskMessage> = socketBuilder
        .onMessage
        .map {
            Log.d("taskListMessage", "onMap")
            Gson().fromJson<ApiResponse<TaskResponse>>(
                it,
                (object : TypeToken<ApiResponse<TaskResponse>>() {}).type
            )
        }
        .map { res ->
           TaskMessage(
               list = res.data.smsList.map {
                   TaskEntity(
                       id = it.id,
                       sendTo = it.msisdn,
                       message = it.txt,
                       simSlot = if (res.data.sim == "msisdn_1")
                           0
                        else
                            1
                   )
               }
           )
        }
        .onEach {
            save(it.list)
        }
        .catch {
            Log.d("taskListMessage", "catch")
        }
        .onStart {
            Log.d("taskListMessage", "onStart")
            socketBuilder.connect()
        }
        .onCompletion {
            Log.d("taskListMessage", "onCompletion")
            socketBuilder.disconnect()
        }

    override suspend fun sendTaskStatus(taskID: Int) {
        val task = task(taskID)

        val req = TaskStatusRequest(
            data = TaskStatusDataRequest(
                status = when (task.status) {
                    TaskStatus.PROCESS -> "processed"
                    TaskStatus.DELIVERED -> "delivered"
                    TaskStatus.BUFFERED -> "received"
                    else -> "undelivered"
                },
                id = task.id,
                simId = if (task.simSlot == 1)
                    "msisdn_2"
                else "msisdn_1"
            )
        )

        socketBuilder.sendMessage(Gson().toJson(
            req
        ))
    }

    override fun serverConnectStatus(): StateFlow<Boolean> = socketBuilder.statusConnect

}

data class TaskStatusRequest(
    val method: String = "SMS_STATUS",
    val unique_id: String = SmsBlockerDatabase.deviceID,
    val data: TaskStatusDataRequest
)

data class TaskStatusDataRequest(
    val status: String,
    val id: Int,
    val simId: String
)

