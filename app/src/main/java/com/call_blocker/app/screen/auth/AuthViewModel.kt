package com.call_blocker.app.screen.auth

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.call_blocker.app.BuildConfig
import com.call_blocker.common.Resource
import com.call_blocker.repository.RepositoryImp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val userRepository = RepositoryImp.userRepository

    val isLoading = MutableLiveData(false)

    val isSuccessLogin = MutableLiveData<Boolean?>(null)

    private val _resetStatus = mutableStateOf<ResetState>(ResetState.None)
    val resetStatus: State<ResetState> = _resetStatus

    fun login(email: String, password: String) = viewModelScope.launch(Dispatchers.IO) {
        isSuccessLogin.postValue(null)

        isLoading.postValue(true)

        val isSuccess = userRepository.login(
            email, password, BuildConfig.VERSION_NAME
        )

        isLoading.postValue(false)

        isSuccessLogin.postValue(isSuccess)
    }

    fun register(email: String, password: String, packageName: String) =
        viewModelScope.launch(Dispatchers.IO) {
            isSuccessLogin.postValue(null)

            isLoading.postValue(true)

            val isSuccess = userRepository.register(
                email, password, packageName, BuildConfig.VERSION_NAME
            )

            isLoading.postValue(false)

            isSuccessLogin.postValue(isSuccess)
        }

    fun resetPass(email: String) {
        userRepository.reset(email).onEach {
            when (it) {
                is Resource.Success -> {
                    _resetStatus.value = ResetState.Success
                }

                is Resource.Loading -> {
                    _resetStatus.value = ResetState.Loading
                }

                is Resource.Error -> {
                    _resetStatus.value = ResetState.Error(error = it.message ?: "")
                }

                Resource.None -> Unit
            }
        }.launchIn(viewModelScope)
    }

}

sealed class ResetState {
    object Loading : ResetState()
    class Error(val error: String) : ResetState()
    object None : ResetState()
    object Success : ResetState()
}