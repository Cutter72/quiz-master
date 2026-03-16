package pl.pdgroup.quiz.data.mapper

import android.text.Html
import pl.pdgroup.quiz.data.remote.model.QuestionDto
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.Question

fun QuestionDto.toDomain(): Question {
    return Question(
        type = type,
        difficulty = try { Difficulty.valueOf(difficulty.uppercase()) } catch (e: Exception) { Difficulty.EASY },
        category = category,
        question = Html.fromHtml(question, Html.FROM_HTML_MODE_LEGACY).toString(),
        correctAnswer = Html.fromHtml(correctAnswer, Html.FROM_HTML_MODE_LEGACY).toString(),
        incorrectAnswers = incorrectAnswers.map { Html.fromHtml(it, Html.FROM_HTML_MODE_LEGACY).toString() }
    )
}