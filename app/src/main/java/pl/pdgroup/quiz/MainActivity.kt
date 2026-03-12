package pl.pdgroup.quiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import pl.pdgroup.quiz.presentation.navigation.AppNavigation
import pl.pdgroup.quiz.ui.theme.QuizTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeState by viewModel.themeState.collectAsState()
            val navController = rememberNavController()

            QuizTheme(
                darkTheme = themeState.isDarkMode,
                highContrast = themeState.isHighContrast
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        navController = navController,
                        isDarkMode = themeState.isDarkMode,
                        isHighContrast = themeState.isHighContrast,
                        onToggleTheme = { viewModel.toggleTheme() },
                        onToggleContrast = { viewModel.toggleContrast() }
                    )
                }
            }
        }
    }
}