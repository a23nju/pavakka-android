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
import ai.laennec.pavakka.features.achievements.AchievementsScreen
import ai.laennec.pavakka.features.auth.viewmodel.AuthViewModel
import ai.laennec.pavakka.features.coach.CoachScreen
import ai.laennec.pavakka.features.dashboard.ui.DashboardScreen
import ai.laennec.pavakka.features.onboarding.OnboardingScreen
import ai.laennec.pavakka.features.workout.ui.ExerciseAIScreen
import ai.laennec.pavakka.features.diary.ui.DiaryScreen
import ai.laennec.pavakka.features.fasting.ui.FastingScreen
import ai.laennec.pavakka.features.foods.FoodsScreen
import ai.laennec.pavakka.features.goals.GoalsScreen
import ai.laennec.pavakka.features.more.MoreScreen
import ai.laennec.pavakka.features.plan.PlanScreen
import ai.laennec.pavakka.features.progress.ui.ProgressScreen
import ai.laennec.pavakka.features.report.ReportScreen
import ai.laennec.pavakka.features.scan.ScanScreen
import ai.laennec.pavakka.features.workout.ui.WorkoutScreen

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Filled.Home)
    object Diary : Screen("diary", "Diary", Icons.Filled.DateRange)
    object Progress : Screen("progress", "Progress", Icons.Filled.Star)
    object Workout : Screen("workout", "Workout", Icons.Filled.PlayArrow)
    object More : Screen("more", "More", Icons.Filled.Menu)
}

@Composable
fun MainNavHost(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val tabs = listOf(Screen.Dashboard, Screen.Diary, Screen.Progress, Screen.Workout, Screen.More)
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val showBar = currentRoute in tabs.map { it.route }

    Scaffold(
        floatingActionButton = {
            // Floating AI Coach chat bubble — shown on tabs that don't have their own FAB.
            val noOwnFab = currentRoute == Screen.Dashboard.route ||
                currentRoute == Screen.Progress.route ||
                currentRoute == Screen.More.route
            if (showBar && noOwnFab) {
                FloatingActionButton(
                    onClick = { navController.navigate("coach") },
                    containerColor = ai.laennec.pavakka.core.ui.theme.BrandGreen
                ) {
                    androidx.compose.material3.Text("💬", fontSize = androidx.compose.ui.unit.TextUnit(22f, androidx.compose.ui.unit.TextUnitType.Sp))
                }
            }
        },
        bottomBar = {
            if (showBar) {
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
        }
    ) { padding ->
        NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
            composable(Screen.Dashboard.route) { DashboardScreen(authViewModel) }
            composable(Screen.Diary.route) { DiaryScreen() }
            composable(Screen.Progress.route) { ProgressScreen() }
            composable(Screen.Workout.route) { WorkoutScreen() }
            composable(Screen.More.route) {
                MoreScreen(authViewModel) { route -> navController.navigate(route) }
            }
            // Sub-routes reachable from More
            composable("fasting") { FastingScreen() }
            composable("goals") { GoalsScreen() }
            composable("foods") { FoodsScreen() }
            composable("plan") { PlanScreen() }
            composable("scan") { ScanScreen() }
            composable("report") { ReportScreen() }
            composable("coach") { CoachScreen() }
            composable("achievements") { AchievementsScreen() }
            composable("onboarding") { OnboardingScreen() }
            composable("reminders") { ai.laennec.pavakka.features.reminders.RemindersScreen() }
            composable("exerciseAi") { ExerciseAIScreen() }
        }
    }
}
