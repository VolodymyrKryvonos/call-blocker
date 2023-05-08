package com.call_blocker.app.new_ui

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.call_blocker.app.R
import com.call_blocker.app.new_ui.navigation.BottomNavGraph
import com.call_blocker.app.new_ui.navigation.Routes
import com.call_blocker.app.new_ui.screens.home_screen.HomeViewModel
import com.call_blocker.app.new_ui.screens.login_screen.AuthorizationViewModel
import com.call_blocker.app.new_ui.screens.login_screen.LoginScreen
import com.call_blocker.app.new_ui.screens.sim_card_info_screen.SimCardViewModel
import com.call_blocker.app.new_ui.screens.task_screen.TasksViewModel
import com.call_blocker.app.new_ui.widgets.AlertDialog
import com.call_blocker.app.new_ui.widgets.IconWithBackground
import com.call_blocker.app.screen.Banner
import com.call_blocker.app.screen.SplashViewModel
import com.call_blocker.app.screen.intro.IntroScreen
import com.call_blocker.db.SmsBlockerDatabase
import com.call_blocker.ussd_sender.UssdService
import kotlinx.coroutines.delay

class HolderActivity : ComponentActivity() {
    private val homeViewModel: HomeViewModel by viewModels()
    private val simCardViewModel: SimCardViewModel by viewModels()
    private val splashViewModel: SplashViewModel by viewModels()
    private val tasksViewModel: TasksViewModel by viewModels()
    private val authorizationViewModel: AuthorizationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashViewModel.initMe(this)
        simCardViewModel.simsInfo(this)
        setContent {
            val isUserAuthorized by SmsBlockerDatabase
                .userIsAuthLiveData
                .collectAsState(initial = SmsBlockerDatabase.userToken != null)
            val isPermissionsGranted by splashViewModel.isPermissionGranted.observeAsState(initial = null)
            val isAppDefault by splashViewModel.isAppDefault.observeAsState(initial = null)
            val navController: NavHostController = rememberNavController()
            Log.e("isAppDefault", "$isAppDefault")
            Log.e("isPermissionsGranted", "$isPermissionsGranted")
            if (!isUserAuthorized) {
                Them {
                    LoginScreen(authorizationViewModel)
                }
            } else {
                if (isPermissionsGranted == true && isAppDefault == true)
                    Scaffold(bottomBar = { Them { BottomBar(navController = navController) } }) { padding ->
                        Them {
                            Box(modifier = Modifier.padding(padding)) {
                                BottomNavGraph(
                                    navController,
                                    homeViewModel = homeViewModel,
                                    simCardViewModel = simCardViewModel,
                                    tasksViewModel = tasksViewModel
                                )
                            }
                        }
                    }
                else if (isPermissionsGranted == false || isAppDefault == false)
                    Them { IntroScreen(mViewModel = splashViewModel) }
                else
                    Them { Banner(splashViewModel) }

            }
            UpdateAppVersionDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        homeViewModel.checkIsLatestVersion()
        SmsBlockerDatabase.isUssdCommandOn = UssdService.hasAccessibilityPermission(this)
    }

    @Composable
    private fun UpdateAppVersionDialog() {
        Them {
            if (homeViewModel.state.showUpdateAppDialog) {
                val profile = SmsBlockerDatabase.profile
                AlertDialog(
                    title = stringResource(id = R.string.update_application),
                    message = stringResource(
                        id = R.string.update_application_to_continue,
                        profile?.latestMajorVersion ?: 0,
                        profile?.latestMinorVersion ?: 0,
                        profile?.latestPatchVersion ?: 0
                    ),
                    modifier = Modifier.fillMaxSize(),
                    onClose = {
                        homeViewModel.closeUpdateDialog()
                    },
                    content = {
                        Button(
                            onClick = {
                                homeViewModel.closeUpdateDialog()
                                val url = "https://free-tokens.info/download_the_latest_app"
                                val i = Intent(Intent.ACTION_VIEW)
                                i.data = Uri.parse(url)
                                startActivity(i)

                            },
                            shape = RoundedCornerShape(100f),
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = buttonBackground,
                                disabledBackgroundColor = disabledButton
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 5.dp),
                        ) {
                            Text(
                                modifier = Modifier.padding(horizontal = 6.dp),
                                text = stringResource(R.string.ok),
                                style = MaterialTheme.typography.h5,
                                color = buttonTextColor
                            )
                        }
                    }
                )
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
                backgroundColor = MaterialTheme.colors.onBackground,
                contentColor = primary
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
                    color = primary,
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
        val isSelected = currentDestination?.hierarchy?.any {
            it.route == route.destination
        } == true
        BottomNavigationItem(
            label = {
                Text(
                    text = stringResource(id = route.titleId),
                    style = tabFont,
                    color = tabTextColor
                )
            },
            icon = {
                if (isSelected) {
                    IconWithBackground(
                        iconDrawable = route.iconId,
                        contentDescription = "Navigation Icon",
                        tint = tabIconColor,
                        background = lightBlue,
                        paddingVertical = 0.dp,
                        paddingHorizontal = 14.dp
                    )
                } else {
                    Icon(
                        painterResource(id = route.iconId),
                        contentDescription = "Navigation Icon",
                        tint = tabIconColor
                    )
                }
            },
            selected = isSelected,
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