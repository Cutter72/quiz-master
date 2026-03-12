package pl.pdgroup.quiz.presentation.screen.result

import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.presentation.mvi.ViewEffect
import pl.pdgroup.quiz.presentation.mvi.ViewIntent
import pl.pdgroup.quiz.presentation.mvi.ViewState

class ResultContract {
    data class State(
        val category: String = "",
        val difficulty: Difficulty = Difficulty.EASY,
        val score: Int = 0,
        val totalQuestions: Int = 0,
        val percentage: Int = 0,
        val isSaved: Boolean = false
    ) : ViewState

    sealed class Intent : ViewIntent {
        object TryAnotherQuiz : Intent()
        object ViewScoreboard : Intent()
        object BackToHome : Intent()
    }

    sealed class Effect : ViewEffect {
        object NavigateToSelection : Effect()
        object NavigateToScoreboard : Effect()
        object NavigateToHome : Effect()
    }
}