package com.call_blocke.app.screen.auth

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.call_blocke.app.BuildConfig
import com.call_blocke.repository.RepositoryImp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val userRepository = RepositoryImp.userRepository

    val isLoading = MutableLiveData(false)

    val isSuccessLogin = MutableLiveData<Boolean?>(null)

    fun login(email: String, password: String) = viewModelScope.launch(Dispatchers.IO) {
        isSuccessLogin.postValue(null)

        isLoading.postValue(true)

        val isSuccess = userRepository.login(
            email, password, BuildConfig.VERSION_NAME
        )

        isLoading.postValue(false)

        isSuccessLogin.postValue(isSuccess)
    }

    fun register(email: String, password: String) = viewModelScope.launch(Dispatchers.IO) {
        isSuccessLogin.postValue(null)

        isLoading.postValue(true)

        val isSuccess = userRepository.register(
            email, password
        )

        isLoading.postValue(false)

        isSuccessLogin.postValue(isSuccess)
    }

}