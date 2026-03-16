package pl.pdgroup.quiz.domain.usecase

import pl.pdgroup.quiz.domain.model.QuizScore
import pl.pdgroup.quiz.domain.repository.QuizRepository
import java.time.Instant
import javax.inject.Inject

class SaveScoreUseCase @Inject constructor(
    private val repository: QuizRepository
) {
    suspend operator fun invoke(category: String, difficulty: pl.pdgroup.quiz.domain.model.Difficulty, score: Int, totalQuestions: Int) {
        val dateString = Instant.now().toString()
        
        val quizScore = QuizScore(
            category = category,
            difficulty = difficulty,
            score = score,
            totalQuestions = totalQuestions,
            date = dateString
        )
        repository.saveScore(quizScore)
    }
}