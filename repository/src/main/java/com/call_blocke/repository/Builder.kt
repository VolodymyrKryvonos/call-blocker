package com.call_blocke.repository

import android.content.Context
import com.call_blocke.a_repository.repository.SettingsRepositoryImp
import com.call_blocke.a_repository.repository.TaskRepositoryImp
import com.call_blocke.a_repository.repository.UserRepositoryImp
import com.call_blocke.rest_work_imp.RepositoryBuilder
import com.call_blocke.rest_work_imp.SettingsRepository
import com.call_blocke.rest_work_imp.TaskRepository
import com.call_blocke.rest_work_imp.UserRepository

object RepositoryImp {

    val userRepository: UserRepository by lazy {
        UserRepositoryImp()
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepositoryImp()
    }

    val taskRepository: TaskRepository by lazy {
        TaskRepositoryImp()
    }

    fun init(context: Context) {
        RepositoryBuilder.init(context)
    }

}