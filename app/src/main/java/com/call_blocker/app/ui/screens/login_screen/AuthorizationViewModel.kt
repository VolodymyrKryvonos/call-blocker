package com.call_blocker.app.ui.screens.login_screen

import android.content.Context
import android.util.Patterns
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.call_blocker.app.BuildConfig
import com.call_blocker.db.SmsBlockerDatabase
import com.call_blocker.loger.SmartLog
import com.call_blocker.rest_work_imp.LogRepository
import com.call_blocker.rest_work_imp.UserRepository
import kotlinx.coroutines.launch

class AuthorizationViewModel(
    private val userRepository: UserRepository,
    private val logRepository: LogRepository,
    private val smsBlockerDatabase: SmsBlockerDatabase
) : ViewModel() {
    var isSignUp: Boolean by mutableStateOf(false)
    var email: String by mutableStateOf("")
    var emailError by mutableStateOf(false)
    var password: String by mutableStateOf("")
    var whatsApp: String by mutableStateOf("")
    var whatsAppError by mutableStateOf(false)
    var passwordError by mutableStateOf(false)
    var confirmPassword: String by mutableStateOf("")
    var confirmPasswordError by mutableStateOf(false)
    var isLoading by mutableStateOf(false)


    fun isEmailValid() {
        emailError = email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()
        SmartLog.e("isEmailValid emailError = $emailError")
    }

    fun signIn(packageName: String, context: Context) {
        viewModelScope.launch {
            isLoading = true
            validatePassword()
            isWhatsAppNumberValid()
            isEmailValid()
            var isSignedIn = false
            if (isSignUp) {
                if (!passwordError && !confirmPasswordError && !whatsAppError) {
                    isSignedIn = userRepository.register(
                        email, password, whatsApp, packageName, BuildConfig.VERSION_NAME
                    )
                }
            } else {
                if (!passwordError) {
                    isSignedIn = userRepository.login(
                        email, password, BuildConfig.VERSION_NAME
                    )
                }
            }
            passwordError = !isSignedIn
            isLoading = false
        }

    }

    private fun validatePassword() {
        passwordError = password.length < 3 || password.length > 30
        if (isSignUp) {
            confirmPasswordError = password != confirmPassword
        }
        SmartLog.e("validatePassword passwordError = $passwordError confirmPasswordError = $confirmPasswordError")
    }

    fun isWhatsAppNumberValid() {
        whatsAppError = whatsApp.isEmpty() || whatsApp.length < 8 || whatsApp.length > 15
        SmartLog.e("isWhatsAppNumberValid whatsAppError $whatsAppError")
    }
}
 