package com.call_blocke.app.new_ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.call_blocke.app.new_ui.screens.SettingsScreen
import com.call_blocke.app.new_ui.screens.home_screen.HomeScreen
import com.call_blocke.app.new_ui.screens.home_screen.HomeViewModel
import com.call_blocke.app.new_ui.screens.sim_card_info_screen.SimCardInfoScreen
import com.call_blocke.app.new_ui.screens.sim_card_info_screen.SimCardViewModel
import com.call_blocke.app.new_ui.screens.task_screen.TaskScreen
import com.call_blocke.app.new_ui.screens.task_screen.TasksViewModel

@Composable
fun BottomNavGraph(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    simCardViewModel: SimCardViewModel,
    tasksViewModel: TasksViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Routes.BottomNavigation.HomeScreen.destination
    ) {
        composable(route = Routes.BottomNavigation.HomeScreen.destination) {
            HomeScreen(homeViewModel, navController)
        }
        composable(
            route = Routes.BottomNavigation.SimInfoScreen.destination,
            arguments = Routes.BottomNavigation.SimInfoScreen.arguments
        ) {
            SimCardInfoScreen(simCardViewModel, it.arguments?.getInt("SimSlot") ?: 0)
        }
        composable(route = Routes.BottomNavigation.SettingsScreen.destination) {
            SettingsScreen()
        }
        composable(route = Routes.BottomNavigation.TaskListScreen.destination) {
            TaskScreen(tasksViewModel)
        }
    }
}