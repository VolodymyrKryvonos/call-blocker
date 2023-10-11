package com.call_blocker.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.call_blocker.app.ui.UiEvent
import com.call_blocker.app.ui.screens.home_screen.HomeScreen
import com.call_blocker.app.ui.screens.home_screen.HomeScreenState
import com.call_blocker.app.ui.screens.settings_screen.SettingsScreen
import com.call_blocker.app.ui.screens.settings_screen.SettingsScreenState
import com.call_blocker.app.ui.screens.sim_card_info_screen.SimCardInfoEvents
import com.call_blocker.app.ui.screens.sim_card_info_screen.SimCardInfoScreen
import com.call_blocker.app.ui.screens.sim_card_info_screen.SimCardInfoScreenState
import com.call_blocker.app.ui.screens.task_screen.TaskScreen
import com.call_blocker.app.ui.screens.task_screen.TasksScreenState

@Composable
fun BottomNavGraph(
    navController: NavHostController,
    homeState: HomeScreenState,
    simCardState: SimCardInfoScreenState,
    tasksState: TasksScreenState,
    settingsState: SettingsScreenState,
    handleEvent: (UiEvent) -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Routes.BottomNavigation.HomeScreen.destination
    ) {
        composable(route = Routes.BottomNavigation.HomeScreen.destination) {
            HomeScreen(
                homeState,
                onEvent = handleEvent,
                onNewDestination = navController::navigate
            )
        }
        composable(
            route = Routes.BottomNavigation.SimInfoScreen.destination,
            arguments = Routes.BottomNavigation.SimInfoScreen.arguments
        ) {
            LaunchedEffect(key1 = it.arguments?.getInt("SimSlot") ?: 0) {
                handleEvent(
                    SimCardInfoEvents.SetCurrentPageEvent(
                        it.arguments?.getInt(
                            "SimSlot"
                        ) ?: 0
                    )
                )
            }
            SimCardInfoScreen(simCardState, onEvent = handleEvent)
        }
        composable(route = Routes.BottomNavigation.SettingsScreen.destination) {
            SettingsScreen(settingsState, handleEvent)
        }
        composable(route = Routes.BottomNavigation.TaskListScreen.destination) {
            TaskScreen(tasksState, handleEvent)
        }
    }
}