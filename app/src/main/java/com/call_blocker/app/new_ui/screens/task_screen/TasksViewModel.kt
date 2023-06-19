package com.call_blocker.app.new_ui.screens.task_screen

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import com.call_blocker.app.R
import com.call_blocker.app.new_ui.BaseViewModel
import com.call_blocker.app.new_ui.UiEvent
import com.call_blocker.app.new_ui.UiState
import com.call_blocker.app.new_ui.primary
import com.call_blocker.app.new_ui.tintConnected
import com.call_blocker.app.new_ui.tintError
import com.call_blocker.db.entity.TaskEntity
import com.call_blocker.db.entity.TaskStatus
import com.call_blocker.rest_work_imp.TaskRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Date

class TasksViewModel(private val taskRepository: TaskRepository) :
    BaseViewModel<TasksScreenState, TasksScreenEvents>() {

    init {
        viewModelScope.launch {
            taskRepository.taskList().map { it.map { it.toTask() } }.collectLatest {
                setState { state.value.copy(taskList = it) }
            }
        }
    }

    override fun setInitialState() = TasksScreenState()

    override fun handleEvent(event: TasksScreenEvents) {
    }
}

data class TasksScreenState(val taskList: List<Task> = listOf()) : UiState
sealed interface TasksScreenEvents : UiEvent

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