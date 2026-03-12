package pl.pdgroup.quiz.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class QuizScore(
    val category: String,
    val difficulty: Difficulty,
    val score: Int,
    val totalQuestions: Int,
    val date: String
)
