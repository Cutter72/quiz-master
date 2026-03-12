package pl.pdgroup.quiz.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import pl.pdgroup.quiz.presentation.screen.home.HomeScreen
import pl.pdgroup.quiz.presentation.screen.quiz.QuizScreen
import pl.pdgroup.quiz.presentation.screen.selection.SelectionScreen

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
            SelectionScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToQuiz = { category, difficulty ->
                    navController.navigate("quiz/$category/${difficulty.name}")
                }
            )
        }
        composable(
            route = "quiz/{category}/{difficulty}",
            arguments = listOf(
                navArgument("category") { type = NavType.StringType },
                navArgument("difficulty") { type = NavType.StringType }
            )
        ) {
            QuizScreen(
                onNavigateBack = { navController.popBackStack("home", inclusive = false) },
                onNavigateToResults = { category, difficulty, score, total ->
                    navController.navigate("result/$category/${difficulty.name}/$score/$total") {
                        popUpTo("home") { inclusive = false }
                    }
                }
            )
        }
        composable(
            route = "result/{category}/{difficulty}/{score}/{total}",
            arguments = listOf(
                navArgument("category") { type = NavType.StringType },
                navArgument("difficulty") { type = NavType.StringType },
                navArgument("score") { type = NavType.IntType },
                navArgument("total") { type = NavType.IntType }
            )
        ) {
            // TODO: ResultScreen
        }
        composable("scoreboard") {
            // TODO: ScoreboardScreen
        }
    }
}