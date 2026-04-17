package com.mitv.player.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mitv.player.ui.screens.DashboardScreen
import com.mitv.player.ui.screens.OnboardingScreen
import com.mitv.player.ui.screens.PlayerScreen
import com.mitv.player.ui.screens.SettingsScreen

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Dashboard : Screen("dashboard")
    object Player : Screen("player/{channelIndex}") {
        fun createRoute(index: Int) = "player/$index"
    }
    object Settings : Screen("settings")
}

@Composable
fun MiTVNavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Screen.Dashboard.route) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onDone = { navController.navigate(Screen.Dashboard.route) {
                popUpTo(Screen.Onboarding.route) { inclusive = true }
            }})
        }
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onChannelClick = { index -> navController.navigate(Screen.Player.createRoute(index)) },
                onSettingsClick = { navController.navigate(Screen.Settings.route) }
            )
        }
        composable(
            route = Screen.Player.route,
            arguments = listOf(navArgument("channelIndex") { type = NavType.IntType })
        ) { backStackEntry ->
            val index = backStackEntry.arguments?.getInt("channelIndex") ?: 0
            PlayerScreen(channelIndex = index, onBack = { navController.popBackStack() })
        }
        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
