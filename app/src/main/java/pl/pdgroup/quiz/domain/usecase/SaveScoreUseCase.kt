package pl.pdgroup.quiz.domain.usecase

import pl.pdgroup.quiz.domain.model.QuizScore
import pl.pdgroup.quiz.domain.repository.QuizRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

class SaveScoreUseCase @Inject constructor(
    private val repository: QuizRepository
) {
    suspend operator fun invoke(category: String, difficulty: pl.pdgroup.quiz.domain.model.Difficulty, score: Int, totalQuestions: Int) {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        sdf.timeZone = TimeZone.getTimeZone("UTC")
        val dateString = sdf.format(Date())
        
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