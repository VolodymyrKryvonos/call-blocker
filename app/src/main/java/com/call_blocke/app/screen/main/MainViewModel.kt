package com.call_blocke.app.screen.main

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.call_blocke.app.worker_manager.ServiceWorker
import com.call_blocke.db.SmsBlockerDatabase
import com.call_blocke.repository.RepositoryImp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    val taskExecutorIsRunning = ServiceWorker.isRunning

    val systemInfoLiveData = MutableLiveData(SmsBlockerDatabase.systemDetail)

    val isLoading = MutableLiveData(false)

    private val userRepository by lazy {
        RepositoryImp.userRepository
    }

    private val taskRepository by lazy {
        RepositoryImp.taskRepository
    }

    val isPingOn = taskRepository.ping

    val isServerOnline = taskRepository.serverConnectStatus()

    fun userName() = userRepository.userName()

    fun userPassword() = userRepository.userPassword()

    val deviceID = userRepository.deviceID

    fun runExecutor(context: Context) {
        ServiceWorker.start(context = context)
    }

    fun stopExecutor(context: Context) {
        ServiceWorker.stop(context = context)
    }

    fun reloadSystemInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading.postValue(true)

            val systemDetail = userRepository.systemDetail()

            isLoading.postValue(false)

            systemInfoLiveData.postValue(systemDetail)
        }
    }

    fun logOut(context: Context) {
        stopExecutor(context = context)
        userRepository.logOut()
    }

}