package com.call_blocker.app.new_ui.screens.task_screen

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import com.call_blocker.app.R
import com.call_blocker.app.new_ui.primary
import com.call_blocker.app.new_ui.tintConnected
import com.call_blocker.app.new_ui.tintError
import com.call_blocker.db.entity.TaskEntity
import com.call_blocker.db.entity.TaskStatus
import com.call_blocker.repository.RepositoryImp
import com.call_blocker.rest_work_imp.TaskRepository
import kotlinx.coroutines.flow.map
import java.util.*

class TasksViewModel : ViewModel() {
    private val taskRepository: TaskRepository by lazy {
        RepositoryImp.taskRepository
    }

    val taskList = taskRepository.taskList().map { it.map { it.toTask() } }
}

fun TaskEntity.toTask() = Task(
    id = id.toString(),
    simSlot = simSlot ?: -1,
    status = when (this.status) {
        TaskStatus.BUFFERED -> R.string.buffered
        TaskStatus.PROCESS -> R.string.processing
        TaskStatus.DELIVERED -> R.string.delivered
        TaskStatus.TIME_RANGE_VIOLATED -> R.string.undelivered
        TaskStatus.ERROR -> R.string.undelivered
    },
    statusColor = when (this.status) {
        TaskStatus.BUFFERED -> primary
        TaskStatus.PROCESS -> primary
        TaskStatus.DELIVERED -> tintConnected
        TaskStatus.TIME_RANGE_VIOLATED -> tintError
        TaskStatus.ERROR -> tintError
    },
    bufferedDate = Date(bufferedAt).toString(),
    proceedDate = Date(processAt).toString(),
    deliveredDate = Date(deliveredAt).toString()
)

data class Task(
    val simSlot: Int,
    val id: String,
    @StringRes
    val status: Int,
    val statusColor: Color,
    val bufferedDate: String? = null,
    val proceedDate: String? = null,
    val deliveredDate: String? = null,
)