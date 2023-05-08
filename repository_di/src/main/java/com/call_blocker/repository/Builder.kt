package com.call_blocker.repository

import android.content.Context
import com.call_blocker.a_repository.repository.*
import com.call_blocker.rest_work_imp.*

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

    val logRepository: LogRepository by lazy {
        LogRepositoryImpl()
    }

    val replyRepository: ReplyRepository by lazy {
        ReplyRepositoryImpl()
    }

    val ussdRepository: UssdRepository by lazy {
        UssdRepositoryImpl()
    }

    fun init(context: Context) {
        RepositoryBuilder.init(context)
    }

}