package pl.pdgroup.quiz.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Question(
    val type: String,
    val difficulty: Difficulty,
    val category: String,
    val question: String,
    @SerialName("correct_answer") val correctAnswer: String,
    @SerialName("incorrect_answers") val incorrectAnswers: List<String>
)

@Serializable
enum class Difficulty {
    @SerialName("easy") EASY,
    @SerialName("medium") MEDIUM,
    @SerialName("hard") HARD
}
