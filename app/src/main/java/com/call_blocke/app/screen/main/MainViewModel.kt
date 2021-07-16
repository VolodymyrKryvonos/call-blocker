package com.call_blocke.app.screen.main

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.*
import com.call_blocke.app.service.TaskExecutorService
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.db.entity.SystemDetailEntity
import com.call_blocke.repository.RepositoryImp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainViewModel : ViewModel() {

    val taskExecutorIsRunning = TaskExecutorService.isRunning

    private val userRepository by lazy {
        RepositoryImp.userRepository
    }

    private val mHandler = Handler(Looper.getMainLooper())

    fun runExecutor(context: Context) {
        TaskExecutorService.start(context = context)
    }

    fun stopExecutor(context: Context) {
        TaskExecutorService.stop(context = context)
    }

    fun systemInfo(): LiveData<SystemDetailEntity> {
        val data = MutableLiveData(SmsBlockerDatabase.systemDetail)

        var run: Runnable? = null

        run = Runnable {
            viewModelScope.launch(Dispatchers.IO) {
                data.postValue(userRepository.systemDetail())
            }
            mHandler.postDelayed(run!!, TimeUnit.SECONDS.toMillis(10L))
        }

        run.run()

        return data
    }

}