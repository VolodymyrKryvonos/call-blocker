package com.call_blocke.app.new_ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.call_blocke.app.R

sealed class Routes(val destination: String, val arguments: List<NamedNavArgument>) {
    sealed class BottomNavigation(
        destination: String,
        @StringRes
        val titleId: Int,
        @DrawableRes
        val iconId: Int,
        arguments: List<NamedNavArgument> = listOf(),
    ) : Routes(destination, arguments) {
        object HomeScreen : BottomNavigation("Home Screen", R.string.home, R.drawable.ic_home)
        object SimInfoScreen : BottomNavigation(
            "Sim Info Screen?SimSlot={SimSlot}",
            R.string.sim,
            R.drawable.ic_sim_card,
            listOf(navArgument("SimSlot") {
                type = NavType.IntType
                defaultValue = 0
            })
        )

        object SettingsScreen :
            BottomNavigation("Settings Screen", R.string.settings, R.drawable.ic_settings)

        object TaskListScreen :
            BottomNavigation("Task List Screen", R.string.task, R.drawable.ic_task)
    }
}