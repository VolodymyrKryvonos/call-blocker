package com.call_blocke.app.screen.main

import android.content.Context
import androidx.lifecycle.ViewModel
import com.call_blocke.app.service.TaskExecutorService

class MainViewModel : ViewModel() {

    val taskExecutorIsRunning = TaskExecutorService.isRunning

    fun runExecutor(context: Context) {
        TaskExecutorService.start(context = context)
    }

    fun stopExecutor(context: Context) {
        TaskExecutorService.stop(context = context)
    }

}