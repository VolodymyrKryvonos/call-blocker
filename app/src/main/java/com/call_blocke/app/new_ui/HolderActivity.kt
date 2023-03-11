package com.call_blocke.app.new_ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.call_blocke.app.new_ui.navigation.BottomNavGraph
import com.call_blocke.app.new_ui.navigation.Routes
import com.call_blocke.app.new_ui.screens.home_screen.HomeViewModel
import com.call_blocke.app.new_ui.screens.sim_card_info_screen.SimCardViewModel
import com.call_blocke.app.screen.auth.AuthScreen
import com.call_blocke.app.screen.auth.AuthViewModel
import com.call_blocke.db.SmsBlockerDatabase

class HolderActivity : ComponentActivity() {
    private val homeViewModel: HomeViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private val simCardViewModel: SimCardViewModel by viewModels()

    @OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val isUserAuthorized by SmsBlockerDatabase
                .userIsAuthLiveData
                .collectAsState(initial = SmsBlockerDatabase.userToken != null)
            val navController: NavHostController = rememberNavController()
            if (!isUserAuthorized) {
                AuthScreen(authViewModel)
            } else {
                Scaffold(bottomBar = { Them { BottomBar(navController = navController) } }) { padding ->
                    Them {
                        Box(modifier = Modifier.padding(padding)) {
                            BottomNavGraph(
                                navController,
                                homeViewModel = homeViewModel,
                                simCardViewModel = simCardViewModel
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun BottomBar(navController: NavHostController) {
        val routes = listOf(
            Routes.BottomNavigation.HomeScreen,
            Routes.BottomNavigation.SimInfoScreen,
            Routes.BottomNavigation.TaskListScreen,
            Routes.BottomNavigation.SettingsScreen,
        )
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination
        val indexOfSelectedTab = routes.indexOfFirst { it.destination == currentDestination?.route }
        Box {
            BottomNavigation(
                backgroundColor = MaterialTheme.colors.onBackground
            ) {
                routes.forEach { route ->
                    AddItem(
                        route = route,
                        currentDestination = currentDestination,
                        navController = navController
                    )
                }
            }
            Indicator(
                Modifier
                    .height(4.dp)
                    .align(Alignment.BottomStart),
                currentTabIndex = indexOfSelectedTab,
                tabsCount = routes.size
            )

        }
    }

    @Composable
    fun Indicator(
        modifier: Modifier = Modifier,
        currentTabIndex: Int,
        tabsCount: Int
    ) {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        val tabWidth = screenWidth / tabsCount
        val tabPadding = tabWidth * 0.35f
        val indicatorOffset = (tabWidth * currentTabIndex) + (tabPadding / 2)
        Box(
            modifier = modifier
                .width(tabWidth - tabPadding)
                .offset(x = indicatorOffset)
                .background(Color.Black)
        )
    }

    @Composable
    fun RowScope.AddItem(
        route: Routes.BottomNavigation,
        currentDestination: NavDestination?,
        navController: NavHostController
    ) {
        BottomNavigationItem(
            label = {
                Text(
                    text = stringResource(id = route.titleId),
                    style = tabFont,
                    color = tabTextColor
                )
            },
            icon = {
                Icon(
                    painterResource(id = route.iconId),
                    contentDescription = "Navigation Icon",
                    tint = tabIconColor
                )
            },
            selected = currentDestination?.hierarchy?.any {
                it.route == route.destination
            } == true,
            onClick = {
                navController.navigate(route.destination) {
                    popUpTo(navController.graph.findStartDestination().id)
                    launchSingleTop = true
                }
            },
        )
    }

}