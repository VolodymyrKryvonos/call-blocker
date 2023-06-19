package com.call_blocker.app.new_ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.call_blocker.app.new_ui.screens.home_screen.HomeScreen
import com.call_blocker.app.new_ui.screens.home_screen.HomeViewModel
import com.call_blocker.app.new_ui.screens.settings_screen.SettingsScreen
import com.call_blocker.app.new_ui.screens.settings_screen.SettingsViewModel
import com.call_blocker.app.new_ui.screens.sim_card_info_screen.SimCardInfoEvents
import com.call_blocker.app.new_ui.screens.sim_card_info_screen.SimCardInfoScreen
import com.call_blocker.app.new_ui.screens.sim_card_info_screen.SimCardViewModel
import com.call_blocker.app.new_ui.screens.task_screen.TaskScreen
import com.call_blocker.app.new_ui.screens.task_screen.TasksViewModel

@Composable
fun BottomNavGraph(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    simCardViewModel: SimCardViewModel,
    tasksViewModel: TasksViewModel,
    settingsViewModel: SettingsViewModel
) {
    val homeState = homeViewModel.state.collectAsState()
    val simCardState = simCardViewModel.state.collectAsState()
    val tasksState = tasksViewModel.state.collectAsState()
    val settingsState = settingsViewModel.state.collectAsState()
    NavHost(
        navController = navController,
        startDestination = Routes.BottomNavigation.HomeScreen.destination
    ) {
        composable(route = Routes.BottomNavigation.HomeScreen.destination) {
            HomeScreen(
                homeState.value,
                onEvent = homeViewModel::handleEvent,
                onNewDestination = navController::navigate
            )
        }
        composable(
            route = Routes.BottomNavigation.SimInfoScreen.destination,
            arguments = Routes.BottomNavigation.SimInfoScreen.arguments
        ) {
            simCardViewModel.handleEvent(
                SimCardInfoEvents.SetCurrentPageEvent(
                    it.arguments?.getInt(
                        "SimSlot"
                    ) ?: 0
                )
            )
            SimCardInfoScreen(simCardState.value) { event ->
                simCardViewModel.handleEvent(event)
            }
        }
        composable(route = Routes.BottomNavigation.SettingsScreen.destination) {
            SettingsScreen(settingsState.value, settingsViewModel::handleEvent)
        }
        composable(route = Routes.BottomNavigation.TaskListScreen.destination) {
            TaskScreen(tasksState.value, tasksViewModel::handleEvent)
        }
    }
}