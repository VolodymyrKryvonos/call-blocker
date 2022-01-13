package com.call_blocke.rest_work_imp

import com.call_blocke.db.Preference
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.db.entity.TaskEntity
import com.call_blocke.db.entity.TaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.*

abstract class TaskRepository {

    public var preference: Preference? = null

    val ping = MutableSharedFlow<Boolean>()

    private val taskDao = SmsBlockerDatabase.taskDao

    private val replayTaskDao = SmsBlockerDatabase.replayDao

    protected suspend fun task(id: Int) = taskDao.findByID(id)!!

    protected suspend fun taskAsNull(id: Int) = taskDao.findByID(id)

    protected abstract suspend fun loadTasks(): List<TaskEntity>

    protected abstract suspend fun confirmTask(data: List<TaskEntity>)

    suspend fun reloadTasks() {
        //confirmTasksStatus()

        val tasks = try {
            loadTasks()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }

        taskDao.save(tasks)
    }

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

    suspend fun toProcessList() = taskDao.toProcessList()

    suspend fun deleteUnProcessed() = taskDao.deleteUnProcessed()

    suspend fun confirmTasksStatus(toConfirmList: List<TaskEntity>) {
        if (toConfirmList.isEmpty())
            return

        try {
            confirmTask(toConfirmList)
        } catch (e: Exception) {
            e.printStackTrace()
            return
        }

        toConfirmList.forEach { task ->
            updateTask(task.apply {
                confirmAt = Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis
            })
        }
    }

    fun taskList() = taskDao.taskList().asPagingSourceFactory()

    suspend fun findReplay(rInMsisdn: String, tText: String) = replayTaskDao.find(rInMsisdn, tText)

    suspend fun clearReplay() = replayTaskDao.deleteAll()

    suspend fun replayInPhoneList() = replayTaskDao.rInPhoneList()

    abstract val taskMessage: Flow<TaskMessage>

    abstract suspend fun sendTaskStatus(taskID: Int)

    abstract suspend fun sendTaskStatuses()

    suspend fun deliveredCountToday(simIndex: Int): Int {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

        val from = cal.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val end = cal.apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 900)
        }.timeInMillis

        return taskDao.deliveredCountBetweenFoeSim(
            simIndex = simIndex,
            from = from,
            end = end
        )
    }

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