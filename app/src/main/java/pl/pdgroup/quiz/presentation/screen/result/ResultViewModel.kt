package pl.pdgroup.quiz.presentation.screen.result

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.usecase.SaveScoreUseCase
import pl.pdgroup.quiz.presentation.mvi.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class ResultViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val saveScoreUseCase: SaveScoreUseCase
) : BaseViewModel<ResultContract.State, ResultContract.Intent, ResultContract.Effect>() {

    override fun createInitialState(): ResultContract.State = ResultContract.State()

    init {
        val category = savedStateHandle.get<String>("category") ?: ""
        val difficultyStr = savedStateHandle.get<String>("difficulty") ?: "EASY"
        val difficulty = try {
            Difficulty.valueOf(difficultyStr.uppercase())
        } catch (e: Exception) {
            Difficulty.EASY
        }
        val score = savedStateHandle.get<Int>("score") ?: 0
        val total = savedStateHandle.get<Int>("total") ?: 1
        val percentage = if (total > 0) ((score.toFloat() / total) * 100).toInt() else 0

        setState {
            copy(
                category = category,
                difficulty = difficulty,
                score = score,
                totalQuestions = total,
                percentage = percentage
            )
        }

        val hasSaved = savedStateHandle.get<Boolean>("hasSaved") ?: false
        if (!hasSaved) {
            viewModelScope.launch {
                try {
                    saveScoreUseCase(category, difficulty, score, total)
                    savedStateHandle["hasSaved"] = true
                    setState { copy(isSaved = true) }
                } catch (e: Exception) {
                    // Ignore error for now, maybe log it
                }
            }
        } else {
            setState { copy(isSaved = true) }
        }
    }

    override suspend fun processIntent(intent: ResultContract.Intent) {
        when (intent) {
            ResultContract.Intent.TryAnotherQuiz -> setEffect { ResultContract.Effect.NavigateToSelection }
            ResultContract.Intent.ViewScoreboard -> setEffect { ResultContract.Effect.NavigateToScoreboard }
            ResultContract.Intent.BackToHome -> setEffect { ResultContract.Effect.NavigateToHome }
        }
    }
}