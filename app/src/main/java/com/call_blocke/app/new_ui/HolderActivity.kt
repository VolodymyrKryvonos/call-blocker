package com.call_blocke.app.new_ui

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
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
import com.call_blocke.app.screen.Banner
import com.call_blocke.app.screen.SplashViewModel
import com.call_blocke.app.screen.auth.AuthScreen
import com.call_blocke.app.screen.auth.AuthViewModel
import com.call_blocke.app.screen.intro.IntroScreen
import com.call_blocke.db.SmsBlockerDatabase
import kotlinx.coroutines.delay

class HolderActivity : ComponentActivity() {
    private val homeViewModel: HomeViewModel by viewModels()
    private val authViewModel: AuthViewModel by viewModels()
    private val simCardViewModel: SimCardViewModel by viewModels()
    private val splashViewModel: SplashViewModel by viewModels()

    @OptIn(
        ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class,
        ExperimentalMaterialApi::class, ExperimentalFoundationApi::class
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        simCardViewModel.simsInfo(this)
        setContent {
            val isUserAuthorized by SmsBlockerDatabase
                .userIsAuthLiveData
                .collectAsState(initial = SmsBlockerDatabase.userToken != null)
            val isPermissionsGranted by splashViewModel.isPermissionGranted.observeAsState(initial = null)
            val isAppDefault by splashViewModel.isAppDefault.observeAsState(initial = null)
            val navController: NavHostController = rememberNavController()
            if (!isUserAuthorized) {
                AuthScreen(authViewModel)
            } else {
                if (isPermissionsGranted == true && isAppDefault == true)
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
                else if (isPermissionsGranted == false || isAppDefault == false)
                    IntroScreen(mViewModel = splashViewModel)
                else
                    Banner(splashViewModel)

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

    @SuppressLint("UnrememberedMutableState")
    @Composable
    fun Indicator(
        modifier: Modifier = Modifier,
        currentTabIndex: Int,
        tabsCount: Int
    ) {
        var drawingProgress by mutableStateOf(0f)
        LaunchedEffect(currentTabIndex) {
            while (drawingProgress < 1f) {
                delay(5)
                drawingProgress += 0.05f
            }
        }
        Box(modifier) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val tabWidth = size.width / tabsCount
                val tabPadding = tabWidth * 0.35f
                val indicatorOffset = (tabWidth * currentTabIndex) + (tabPadding / 2)
                val tabWidthWithPadding = (tabWidth - tabPadding)
                drawRoundRect(
                    topLeft = Offset(
                        indicatorOffset + (tabWidthWithPadding / 2) * (1 - drawingProgress),
                        0f
                    ),
                    size = Size(width = tabWidthWithPadding * drawingProgress, size.height),
                    color = Color.Black,
                    cornerRadius = CornerRadius(100f, 100f)
                )
            }
        }

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
                if (route.destination != navController.currentDestination?.route) {
                    navController.navigate(route.destination) {
                        popUpTo(navController.graph.findStartDestination().id)
                        launchSingleTop = true
                    }
                }
            },
        )
    }

}