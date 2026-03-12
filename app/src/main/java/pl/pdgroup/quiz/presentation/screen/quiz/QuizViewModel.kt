package pl.pdgroup.quiz.presentation.screen.quiz

import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.lifecycle.HiltViewModel
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.usecase.GetQuestionsUseCase
import pl.pdgroup.quiz.presentation.mvi.BaseViewModel
import javax.inject.Inject

@HiltViewModel
class QuizViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getQuestionsUseCase: GetQuestionsUseCase
) : BaseViewModel<QuizContract.State, QuizContract.Intent, QuizContract.Effect>() {

    override fun createInitialState(): QuizContract.State = QuizContract.State()

    init {
        val category = savedStateHandle.get<String>("category")
        val difficultyStr = savedStateHandle.get<String>("difficulty")
        if (category != null && difficultyStr != null) {
            val difficulty = try {
                Difficulty.valueOf(difficultyStr.uppercase())
            } catch (e: Exception) {
                Difficulty.EASY
            }
            handleIntent(QuizContract.Intent.LoadQuestions(category, difficulty))
        }
    }

    override suspend fun processIntent(intent: QuizContract.Intent) {
        when (intent) {
            is QuizContract.Intent.LoadQuestions -> {
                setState { copy(isLoading = true, category = intent.category, difficulty = intent.difficulty) }
                val result = getQuestionsUseCase(intent.category, intent.difficulty)
                result.onSuccess { questions ->
                    if (questions.isNotEmpty()) {
                        val firstQuestion = questions[0]
                        val shuffledAnswers = (firstQuestion.incorrectAnswers + firstQuestion.correctAnswer).shuffled()
                        setState { 
                            copy(
                                isLoading = false, 
                                questions = questions,
                                currentQuestionIndex = 0,
                                shuffledAnswers = shuffledAnswers
                            ) 
                        }
                    } else {
                        setState { copy(isLoading = false, error = "No questions found") }
                    }
                }.onFailure { e ->
                    setState { copy(isLoading = false, error = e.message) }
                }
            }
            is QuizContract.Intent.SelectAnswer -> {
                val state = state.value
                if (state.isAnswerLocked || state.questions.isEmpty()) return
                
                val currentQuestion = state.questions[state.currentQuestionIndex]
                val isCorrect = intent.answer == currentQuestion.correctAnswer
                val newScore = if (isCorrect) state.score + 1 else state.score
                
                setState { 
                    copy(
                        selectedAnswer = intent.answer,
                        isAnswerLocked = true,
                        score = newScore
                    )
                }
            }
            is QuizContract.Intent.ShowCorrectAnswer -> {
                setState { copy(showCorrectAnswer = true) }
            }
            is QuizContract.Intent.NextQuestion -> {
                val state = state.value
                if (state.isLastQuestion) {
                    setEffect {
                        QuizContract.Effect.NavigateToResults(
                            category = state.category,
                            difficulty = state.difficulty,
                            score = state.score,
                            total = state.questions.size
                        )
                    }
                } else {
                    val nextIndex = state.currentQuestionIndex + 1
                    val nextQuestion = state.questions[nextIndex]
                    val nextShuffledAnswers = (nextQuestion.incorrectAnswers + nextQuestion.correctAnswer).shuffled()
                    
                    setState {
                        copy(
                            currentQuestionIndex = nextIndex,
                            selectedAnswer = null,
                            isAnswerLocked = false,
                            showCorrectAnswer = false,
                            shuffledAnswers = nextShuffledAnswers
                        )
                    }
                }
            }
        }
    }
}