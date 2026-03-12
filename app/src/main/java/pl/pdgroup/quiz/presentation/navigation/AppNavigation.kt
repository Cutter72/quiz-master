package pl.pdgroup.quiz.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import pl.pdgroup.quiz.presentation.screen.home.HomeScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    isDarkMode: Boolean,
    isHighContrast: Boolean,
    onToggleTheme: () -> Unit,
    onToggleContrast: () -> Unit
) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                isDarkMode = isDarkMode,
                isHighContrast = isHighContrast,
                onToggleTheme = onToggleTheme,
                onToggleContrast = onToggleContrast,
                onNavigateToSelection = { navController.navigate("selection") },
                onNavigateToScoreboard = { navController.navigate("scoreboard") }
            )
        }
        composable("selection") {
            // TODO: Implementation
        }
        composable("quiz") {
            // TODO: Implementation
        }
        composable("result") {
            // TODO: Implementation
        }
        composable("scoreboard") {
            // TODO: Implementation
        }
    }
}