package pl.pdgroup.quiz.presentation.screen.selection

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.usecase.GetQuestionsUseCase
import pl.pdgroup.quiz.presentation.mvi.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class SelectionViewModel @Inject constructor(
    private val getQuestionsUseCase: GetQuestionsUseCase
) : BaseViewModel<SelectionContract.State, SelectionContract.Intent, SelectionContract.Effect>() {

    override fun createInitialState(): SelectionContract.State = SelectionContract.State()

    override suspend fun processIntent(intent: SelectionContract.Intent) {
        when (intent) {
            is SelectionContract.Intent.SelectCategory -> {
                setState { copy(selectedCategory = intent.category) }
                updateAvailableQuestions()
            }
            is SelectionContract.Intent.SelectDifficulty -> {
                setState { copy(selectedDifficulty = intent.difficulty) }
                updateAvailableQuestions()
            }
            is SelectionContract.Intent.StartQuiz -> {
                val state = state.value
                val category = state.selectedCategory
                val difficulty = state.selectedDifficulty
                if (category != null && difficulty != null) {
                    setState { copy(isLoading = true, error = null) }
                    // Force refresh to get new questions from API on Start
                    val result = getQuestionsUseCase(category, difficulty, forceRefresh = true)
                    setState { copy(isLoading = false) }
                    result.onSuccess {
                        setEffect { SelectionContract.Effect.NavigateToQuiz(category, difficulty) }
                    }.onFailure { e ->
                        val errorMessage = e.message ?: "Unknown error"
                        setState { copy(error = errorMessage) }
                        setEffect { SelectionContract.Effect.ShowError(errorMessage) }
                    }
                }
            }
            is SelectionContract.Intent.ClearError -> {
                setState { copy(error = null) }
            }
        }
    }

    private fun updateAvailableQuestions() {
        val state = state.value
        if (state.selectedCategory != null && state.selectedDifficulty != null) {
            // We request 7 questions from the API
            setState { copy(availableQuestionsCount = 7) }
        } else {
            setState { copy(availableQuestionsCount = 0) }
        }
    }
}