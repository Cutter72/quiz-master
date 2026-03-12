package pl.pdgroup.quiz.presentation.screen.selection

import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.presentation.mvi.ViewEffect
import pl.pdgroup.quiz.presentation.mvi.ViewIntent
import pl.pdgroup.quiz.presentation.mvi.ViewState

class SelectionContract {
    data class State(
        val categories: List<String> = listOf("Sports", "Science", "History", "Geography", "Entertainment", "Technology"),
        val difficulties: List<Difficulty> = listOf(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD),
        val selectedCategory: String? = null,
        val selectedDifficulty: Difficulty? = null,
        val availableQuestionsCount: Int = 0,
        val isLoading: Boolean = false,
        val error: String? = null
    ) : ViewState {
        val isStartEnabled: Boolean
            get() = selectedCategory != null && selectedDifficulty != null && !isLoading
    }

    sealed class Intent : ViewIntent {
        data class SelectCategory(val category: String) : Intent()
        data class SelectDifficulty(val difficulty: Difficulty) : Intent()
        object StartQuiz : Intent()
        object ClearError : Intent()
    }

    sealed class Effect : ViewEffect {
        data class NavigateToQuiz(val category: String, val difficulty: Difficulty) : Effect()
        data class ShowError(val message: String) : Effect()
    }
}