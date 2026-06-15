package ai.laennec.pavakka.core.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ai.laennec.pavakka.features.auth.viewmodel.AuthViewModel
import ai.laennec.pavakka.features.dashboard.ui.DashboardScreen
import ai.laennec.pavakka.features.diary.ui.DiaryScreen
import ai.laennec.pavakka.features.fasting.ui.FastingScreen
import ai.laennec.pavakka.features.progress.ui.ProgressScreen
import ai.laennec.pavakka.features.workout.ui.WorkoutScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Home)
    object Diary : Screen("diary", "Diary", Icons.Filled.DateRange)
    object Progress : Screen("progress", "Progress", Icons.Filled.Star)
    object Fasting : Screen("fasting", "Fasting", Icons.Filled.Refresh)
    object Workout : Screen("workout", "Workout", Icons.Filled.PlayArrow)
}

@Composable
fun MainNavHost(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val tabs = listOf(Screen.Dashboard, Screen.Diary, Screen.Progress, Screen.Fasting, Screen.Workout)
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label) },
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
            composable(Screen.Dashboard.route) { DashboardScreen(authViewModel) }
            composable(Screen.Diary.route) { DiaryScreen() }
            composable(Screen.Progress.route) { ProgressScreen() }
            composable(Screen.Fasting.route) { FastingScreen() }
            composable(Screen.Workout.route) { WorkoutScreen() }
        }
    }
}
