package com.call_blocker.a_repository.repository

import com.call_blocker.a_repository.model.ResendUnprocessedRequest
import com.call_blocker.a_repository.model.SocketMessage
import com.call_blocker.a_repository.model.TaskResponse
import com.call_blocker.a_repository.rest.TaskRest
import com.call_blocker.a_repository.socket.SocketBuilder
import com.call_blocker.common.rest.Const
import com.call_blocker.db.SmsBlockerDatabase
import com.call_blocker.db.TaskMethod
import com.call_blocker.db.entity.TaskEntity
import com.call_blocker.db.entity.TaskStatus
import com.call_blocker.db.entity.TaskStatusData
import com.call_blocker.loger.SmartLog
import com.call_blocker.loger.utils.getStackTrace
import com.call_blocker.rest_work_imp.TaskMessage
import com.call_blocker.rest_work_imp.TaskRepository
import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TaskRepositoryImp(
    private val taskRest: TaskRest,
    private val moshi: Moshi,
    private val smsBlockerDatabase: SmsBlockerDatabase
) : TaskRepository(smsBlockerDatabase) {

    private val socketBuilder by lazy {
        SocketBuilder
            .Builder()
            .setUserToken(smsBlockerDatabase.userToken ?: "jhfhjlbdhjlf")
            .setUUid(smsBlockerDatabase.deviceID)
            .setIP(Const.sandboxDomain)
            .setPort(Const.port)
            .build(smsBlockerDatabase)
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

    override val connectionStatusFlow: StateFlow<Boolean> by lazy { socketBuilder.connectionStatusFlow }

    override suspend fun taskMessage(): Flow<TaskMessage> = channelFlow {
        socketBuilder.connect()
        withContext(Dispatchers.IO) {
            socketBuilder.messageCollector.receiveAsFlow().collect {
                if (it.isNullOrEmpty()) {
                    return@collect
                }
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
        SmartLog.e("$it")
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
            val type =
                Types.newParameterizedType(SocketMessage::class.java, TaskResponse::class.java)
            val parsedMsg = moshi.adapter<SocketMessage<TaskResponse>>(type).fromJson(msg ?: "")

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
                ).also { save(it.list.filter { taskEntity -> taskEntity.method != TaskMethod.GET_LOGS }) }
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
                    },
                    simIccid = task.simIccId
                ),
                unique_id = smsBlockerDatabase.deviceID
            )

            if (!socketBuilder.sendMessage(
                    moshi.adapter(TaskStatusRequest::class.java).toJson(req)
                )
            ) {
                SmartLog.e("Failed send status $req")
                smsBlockerDatabase.taskStatusDao.insertTaskStatus(
                    TaskStatusData(
                        id = req.data.id,
                        status = req.data.status,
                        simId = req.data.simId,
                        time = req.data.date,
                        simIccid = req.data.simIccid
                    )
                )
            }
        } catch (e: Exception) {
            SmartLog.e(getStackTrace(e))
        }
    }

    override suspend fun sendTaskStatuses() {
        val statues = smsBlockerDatabase.taskStatusDao.getAllTaskStatus()
        val statuesMaped = statues.map {
            TaskStatusRequest(
                data = TaskStatusDataRequest(
                    id = it.id,
                    status = it.status,
                    simId = it.simId,
                    date = it.time,
                    simIccid = it.simIccid
                ),
                unique_id = smsBlockerDatabase.deviceID
            )
        }
        for ((i, status) in statuesMaped.withIndex()) {
            if (socketBuilder.sendMessage(
                    moshi.adapter(TaskStatusRequest::class.java).toJson(status)
                )
            ) {
                smsBlockerDatabase.taskStatusDao.deleteTaskStatus(statues[i])
            } else {
                SmartLog.e("Failed send status $status")
            }
        }
    }

    override fun serverConnectStatus(): StateFlow<Boolean> = socketBuilder.statusConnect

}

data class TaskStatusRequest(
    val method: String = "SMS_STATUS",
    val unique_id: String,
    val data: TaskStatusDataRequest
)

data class TaskStatusDataRequest(
    val status: String,
    val id: Int,
    val simId: String,
    val date: Long,
    @Json(name = "sim_iccid")
    val simIccid: String
)

