package com.call_blocke.app

sealed class Navigation(val destination: String) {
    object MainScreen : Navigation("main")
    object SettingsScreen : Navigation("settings")
    object TaskListScreen : Navigation("task_list")
    object BlackListScreen : Navigation("black_list")
    object ResetSimScreen : Navigation("refresh")
    object SimInfoScreen : Navigation("sim_info")
}
