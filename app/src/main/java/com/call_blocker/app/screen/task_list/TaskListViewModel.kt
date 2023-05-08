package com.call_blocker.app.screen.task_list

import androidx.lifecycle.ViewModel
import com.call_blocker.repository.RepositoryImp
import com.call_blocker.rest_work_imp.TaskRepository

class TaskListViewModel : ViewModel() {

    private val taskRepository: TaskRepository by lazy {
        RepositoryImp.taskRepository
    }

    val taskListPaged = taskRepository.taskList()

}