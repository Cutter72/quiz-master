package pl.pdgroup.quiz.presentation.screen.home

import pl.pdgroup.quiz.presentation.mvi.ViewEffect
import pl.pdgroup.quiz.presentation.mvi.ViewIntent
import pl.pdgroup.quiz.presentation.mvi.ViewState

class HomeContract {
    data class State(
        val isDarkMode: Boolean = false,
        val isHighContrast: Boolean = false
    ) : ViewState

    sealed class Intent : ViewIntent {
        object ToggleTheme : Intent()
        object ToggleContrast : Intent()
        object ClickStartQuiz : Intent()
        object ClickScoreboard : Intent()
    }

    sealed class Effect : ViewEffect {
        object NavigateToSelection : Effect()
        object NavigateToScoreboard : Effect()
    }
}