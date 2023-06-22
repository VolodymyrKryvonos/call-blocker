package com.call_blocker.app.di

import com.call_blocker.app.TaskManager
import com.call_blocker.app.ui.SplashViewModel
import com.call_blocker.app.ui.screens.home_screen.HomeViewModel
import com.call_blocker.app.ui.screens.login_screen.AuthorizationViewModel
import com.call_blocker.app.ui.screens.settings_screen.SettingsViewModel
import com.call_blocker.app.ui.screens.sim_card_info_screen.SimCardViewModel
import com.call_blocker.app.ui.screens.task_screen.TasksViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::HomeViewModel)
    viewModelOf(::AuthorizationViewModel)
    viewModelOf(::SimCardViewModel)
    viewModelOf(::TasksViewModel)
    viewModelOf(::SettingsViewModel)
    viewModelOf(::SplashViewModel)

    single { TaskManager(androidContext(), get(), get(), get(), get(), get(), get()) }
}