package com.call_blocke.app.screen.task_list

import androidx.lifecycle.ViewModel
import com.call_blocke.repository.RepositoryImp
import com.call_blocke.rest_work_imp.TaskRepository

class TaskListViewModel : ViewModel() {

    private val taskRepository: TaskRepository by lazy {
        RepositoryImp.taskRepository
    }

    val taskListPaged = taskRepository.taskList()

}