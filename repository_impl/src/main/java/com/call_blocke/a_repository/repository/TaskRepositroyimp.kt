package com.call_blocke.a_repository.repository

import com.call_blocke.a_repository.Const.domain
import com.call_blocke.a_repository.model.*
import com.call_blocke.a_repository.model.TaskStatusRequest
import com.call_blocke.a_repository.rest.TaskRest
import com.call_blocke.a_repository.socket.SocketBuilder
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.db.TaskMethod
import com.call_blocke.db.entity.TaskEntity
import com.call_blocke.db.entity.TaskStatus
import com.call_blocke.db.entity.TaskStatusData
import com.call_blocke.rest_work_imp.TaskMessage
import com.call_blocke.rest_work_imp.TaskRepository
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rokobit.adstvv_unit.loger.SmartLog
import com.rokobit.adstvv_unit.loger.utils.getStackTrace
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class TaskRepositoryImp : TaskRepository() {

    private val taskRest = ApiRepositoryHelper.createRest(TaskRest::class.java)

    private val socketBuilder by lazy {
        SocketBuilder
            .Builder
            .setUserToken(SmsBlockerDatabase.userToken ?: "jhfhjlbdhjlf")
            .setUUid(SmsBlockerDatabase.deviceID)
            .setIP(
                when (preference?.ipType) {
                    "Production" -> domain
                    else -> preference?.customIp ?: ""
                }
            )
            .build()
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

    override fun reconnect() {
        socketBuilder.reconnect()
    }

    override suspend fun resendReceived() {
        try {
            val unprocessedIdList = getReceivedMessagesID()
            if (unprocessedIdList.isNotEmpty()) {
                deleteReceivedMessages()
                taskRest.getReceivedMessagesID(ResendUnprocessedRequest(ids = unprocessedIdList))
            }
        } catch (e: Exception) {
            SmartLog.e("Failed resend ${getStackTrace(e)}")
        }
    }

    override val connectionStatusFlow: Flow<Boolean> by lazy { socketBuilder.connectionStatusFlow }

    override suspend fun taskMessage(): Flow<TaskMessage> = channelFlow {
        socketBuilder.ip =
            when (preference?.ipType) {
                "Production" -> domain
                else -> preference?.customIp ?: ""
            }
        socketBuilder.connect()
        withContext(Dispatchers.IO) {
            socketBuilder.messageCollector.collect {
                async {
                    SmartLog.d("Receive Message $it")
                    toTaskMessage(it)?.let { taskMessage ->
                        if (taskMessage.list.any { it.simSlot != null && it.simSlot != -1 }) {
                            send(taskMessage)
                        }
                    }
                }.start()
            }
        }
    }.onCompletion {
        if (it != null && it !is CancellationException) {
            SmartLog.e("onCompletion ${getStackTrace(it)}")
            socketBuilder.reconnect()
            return@onCompletion
        }
        SmartLog.e("onCompletion")
        socketBuilder.disconnect()

    }

    private suspend fun toTaskMessage(msg: String?): TaskMessage? {
        try {
            val parsedMsg = Gson().fromJson<SocketMessage<TaskResponse>>(
                msg,
                (object : TypeToken<SocketMessage<TaskResponse>>() {}).type
            )

            if (((parsedMsg != null) && (System.currentTimeMillis() - getDate(
                    parsedMsg.options.dateTime ?: ""
                ).time <= 35 * 60 * 1000))
            ) {
                val simSlot = when (parsedMsg.data?.sim) {
                    "msisdn_1" -> 0
                    "msisdn_2" -> 1
                    else -> return null
                }
                return TaskMessage(
                    list = parsedMsg.data.smsList.map {
                        TaskEntity(
                            id = it.id,
                            sendTo = it.msisdn,
                            message = it.txt,
                            highPriority = false,//it.isHighPriority,
                            simSlot = simSlot,
                            method = TaskMethod.valueOf(
                                parsedMsg.method ?: TaskMethod.UNDEFINED.name
                            ),
                            simIccId = parsedMsg.data.simIccId ?: ""
                        )
                    }
                ).also { save(it.list.filter { taskEntity -> taskEntity.message != "GET_LOGS" }) }
            }
        } catch (e: Exception) {
            SmartLog.e(e)
        }
        return null
    }

    private fun getDate(stringDate: String): Date {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
            return format.parse(stringDate) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    }

    override suspend fun sendTaskStatus(taskID: Int) {
        try {
            val task = task(taskID)
            if (task.id == -1) {
                return
            }
            val req = TaskStatusRequest(
                data = TaskStatusDataRequest(
                    status = when (task.status) {
                        TaskStatus.PROCESS -> "processed"
                        TaskStatus.DELIVERED -> "delivered"
                        TaskStatus.BUFFERED -> "received"
                        TaskStatus.TIME_RANGE_VIOLATED -> "time_range_violated"
                        else -> "undelivered"
                    },
                    id = task.id,
                    simId = when (task.simSlot) {
                        1 -> "msisdn_2"
                        0 -> "msisdn_1"
                        else -> "msisdn_1"
                    },
                    date = when (task.status) {
                        TaskStatus.PROCESS -> task.processAt
                        TaskStatus.DELIVERED -> task.deliveredAt
                        TaskStatus.BUFFERED -> task.bufferedAt
                        else -> Date().time
                    }
                )
            )

            if (!socketBuilder.sendMessage(
                    Gson().toJson(
                        req
                    )
                )
            ) {
                SmartLog.e("Failed send status $req")
                SmsBlockerDatabase.taskStatusDao.insertTaskStatus(
                    TaskStatusData(
                        id = req.data.id,
                        status = req.data.status,
                        simId = req.data.simId,
                        time = req.data.date
                    )
                )
            }
        } catch (e: Exception) {
            SmartLog.e(getStackTrace(e))
        }
    }

    override suspend fun sendTaskStatuses() {
        val statues = SmsBlockerDatabase.taskStatusDao.getAllTaskStatus()
        val statuesMaped = statues.map {
            TaskStatusRequest(
                data = TaskStatusDataRequest(
                    id = it.id,
                    status = it.status,
                    simId = it.simId,
                    date = it.time
                )
            )
        }
        for ((i, status) in statuesMaped.withIndex()) {
            if (socketBuilder.sendMessage(
                    Gson().toJson(
                        status
                    )
                )
            ) {
                SmsBlockerDatabase.taskStatusDao.deleteTaskStatus(statues[i])
            } else {
                SmartLog.e("Failed send status $status")
            }
        }
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
    val simId: String,
    val date: Long
)

