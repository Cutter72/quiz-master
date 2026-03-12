package pl.pdgroup.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import pl.pdgroup.quiz.data.local.SettingsManager
import javax.inject.Inject

data class ThemeState(
    val isDarkMode: Boolean = false,
    val isHighContrast: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsManager: SettingsManager
) : ViewModel() {

    val themeState: StateFlow<ThemeState> = combine(
        settingsManager.isDarkMode,
        settingsManager.isHighContrast
    ) { isDark, isHigh ->
        ThemeState(isDarkMode = isDark, isHighContrast = isHigh)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemeState())

    fun toggleTheme() {
        viewModelScope.launch {
            val current = themeState.value.isDarkMode
            settingsManager.setDarkMode(!current)
        }
    }

    fun toggleContrast() {
        viewModelScope.launch {
            val current = themeState.value.isHighContrast
            settingsManager.setHighContrast(!current)
        }
    }
}