package com.call_blocke.rest_work_imp

import android.util.Log
import com.call_blocke.db.Preference
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.db.TaskMethod
import com.call_blocke.db.entity.TaskEntity
import com.call_blocke.db.entity.TaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

abstract class TaskRepository {

    var preference: Preference? = null

    private val taskDao = SmsBlockerDatabase.taskDao

    protected suspend fun task(id: Int) = taskDao.findByID(id) ?: TaskEntity(-1, TaskMethod.UNDEFINED, "","")

    protected abstract suspend fun confirmTask(data: List<TaskEntity>)

    abstract fun reconnect()

    protected suspend fun save(data: List<TaskEntity>) {
        taskDao.save(data)
        data.forEach { taskEntity ->
            updateTask(taskEntity)
        }
    }

    suspend fun save(taskEntity: TaskEntity) {
        taskDao.save(taskEntity)
        updateTask(taskEntity)
    }

    private suspend fun updateTask(taskEntity: TaskEntity) {
        taskDao.update(taskEntity)
        sendTaskStatus(taskEntity.id)
    }

    suspend fun taskOnProcess(taskEntity: TaskEntity, simSlot: Int) {
        updateTask(taskEntity.apply {
            processAt = Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis
            status = TaskStatus.PROCESS
            this.simSlot = simSlot
        })
    }

    suspend fun taskOnDelivered(taskEntity: TaskEntity) {
        when (taskEntity.simSlot) {
            0 -> {
                SmsBlockerDatabase.smsTodaySentFirstSim++
                Log.e("smsTodaySentFirstSim: ", SmsBlockerDatabase.smsTodaySentFirstSim.toString())
            }
            1 -> {
                SmsBlockerDatabase.smsTodaySentSecondSim++
                Log.e("smsTodaySentFirstSim: ", SmsBlockerDatabase.smsTodaySentSecondSim.toString())
            }
            else -> {}
        }
        updateTask(taskEntity.apply {
            deliveredAt = Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis
            taskEntity.status = TaskStatus.DELIVERED
        })
    }

    suspend fun taskOnError(taskEntity: TaskEntity) {
        updateTask(taskEntity.apply {
            deliveredAt = Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis
            taskEntity.status = TaskStatus.ERROR
        })
    }

    abstract suspend fun resendReceived()

    suspend fun getReceivedMessagesID() = taskDao.getReceivedMessagesID()

    suspend fun deleteReceivedMessages() = taskDao.deleteReceivedMessages()

    fun taskList() = taskDao.taskList().asPagingSourceFactory()


    abstract val connectionStatusFlow: Flow<Boolean>

    abstract suspend fun sendTaskStatus(taskID: Int)

    abstract suspend fun sendTaskStatuses()

    abstract suspend fun taskMessage(): Flow<TaskMessage>

    suspend fun clearFor(simIndex: Int) = taskDao.clearFor(simIndex)

    abstract fun serverConnectStatus(): StateFlow<Boolean>
}

data class TaskMessage(
    var list: List<TaskEntity>,
    var simFirstSent: Int = 0,
    var simSecondSent: Int = 0,
    var simFirstMax: Int = 0,
    var simSecondMax: Int = 0
)