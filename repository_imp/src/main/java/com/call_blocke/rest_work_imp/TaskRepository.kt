package com.call_blocke.rest_work_imp

import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.db.entity.TaskEntity
import com.call_blocke.db.entity.TaskStatus
import java.util.*

abstract class TaskRepository {

    private val taskDao = SmsBlockerDatabase.taskDao

    protected abstract suspend fun loadTasks(): List<TaskEntity>

    protected abstract suspend fun confirmTask(data: TaskEntity)

    suspend fun reloadTasks() {
        confirmTasksStatus()

        val tasks = try {
           loadTasks()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }

        taskDao.save(tasks)
    }

    private suspend fun updateTask(taskEntity: TaskEntity) {
        taskDao.update(taskEntity)
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

    suspend fun confirmTasksStatus() {
        taskDao.toConfirmList().forEach { task ->
            val isOk = try {
                confirmTask(task)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }

            if (isOk) {
                task.apply {
                    confirmAt = Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis
                }.also {
                    updateTask(it)
                }
            }
        }
    }

}