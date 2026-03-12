package pl.pdgroup.quiz.presentation.screen.scoreboard

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pl.pdgroup.quiz.domain.model.QuizScore
import pl.pdgroup.quiz.domain.usecase.GetScoresUseCase
import pl.pdgroup.quiz.presentation.mvi.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class ScoreboardViewModel @Inject constructor(
    private val getScoresUseCase: GetScoresUseCase
) : BaseViewModel<ScoreboardContract.State, ScoreboardContract.Intent, ScoreboardContract.Effect>() {

    override fun createInitialState(): ScoreboardContract.State = ScoreboardContract.State()

    init {
        handleIntent(ScoreboardContract.Intent.LoadScores)
    }

    override suspend fun processIntent(intent: ScoreboardContract.Intent) {
        when (intent) {
            is ScoreboardContract.Intent.LoadScores -> {
                viewModelScope.launch {
                    getScoresUseCase().collectLatest { scores ->
                        val totalQuizzes = scores.size
                        val avg = if (scores.isNotEmpty()) {
                            scores.map { it.score.toDouble() / it.totalQuestions }.average() * 100
                        } else 0.0
                        val best = if (scores.isNotEmpty()) {
                            scores.maxOf { (it.score.toDouble() / it.totalQuestions) * 100 }.toInt()
                        } else 0

                        setState {
                            copy(
                                allScores = scores,
                                totalQuizzes = totalQuizzes,
                                averageScore = avg,
                                bestScore = best,
                                isLoading = false
                            )
                        }
                        filterScores(state.value.selectedCategory, scores)
                    }
                }
            }
            is ScoreboardContract.Intent.SelectCategory -> {
                setState { copy(selectedCategory = intent.category) }
                filterScores(intent.category, state.value.allScores)
            }
        }
    }

    private fun filterScores(category: String, scores: List<QuizScore>) {
        val filtered = if (category == "All") {
            scores
        } else {
            scores.filter { it.category == category }
        }
        setState { copy(filteredScores = filtered) }
    }
}