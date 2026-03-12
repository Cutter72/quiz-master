package pl.pdgroup.quiz.presentation.screen.home

import dagger.hilt.android.lifecycle.HiltViewModel
import pl.pdgroup.quiz.presentation.mvi.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor() : BaseViewModel<HomeContract.State, HomeContract.Intent, HomeContract.Effect>() {

    override fun createInitialState(): HomeContract.State = HomeContract.State()

    override suspend fun processIntent(intent: HomeContract.Intent) {
        when (intent) {
            is HomeContract.Intent.ToggleTheme -> {
                setState { copy(isDarkMode = !isDarkMode) }
            }
            is HomeContract.Intent.ToggleContrast -> {
                setState { copy(isHighContrast = !isHighContrast) }
            }
            is HomeContract.Intent.ClickStartQuiz -> {
                setEffect { HomeContract.Effect.NavigateToSelection }
            }
            is HomeContract.Intent.ClickScoreboard -> {
                setEffect { HomeContract.Effect.NavigateToScoreboard }
            }
        }
    }
}