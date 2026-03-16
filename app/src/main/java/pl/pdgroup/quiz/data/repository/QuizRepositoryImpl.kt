package pl.pdgroup.quiz.data.repository

import android.text.Html
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import pl.pdgroup.quiz.data.local.ScoreDao
import pl.pdgroup.quiz.data.local.ScoreEntity
import pl.pdgroup.quiz.data.remote.api.OpenTdbApi
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.Question
import pl.pdgroup.quiz.domain.model.QuizScore
import pl.pdgroup.quiz.domain.repository.QuizRepository

class QuizRepositoryImpl(
    private val scoreDao: ScoreDao,
    private val openTdbApi: OpenTdbApi
) : QuizRepository {

    private var cachedQuestions: List<Question>? = null
    private var lastCategory: String? = null
    private var lastDifficulty: Difficulty? = null

    override suspend fun getQuestions(category: String, difficulty: Difficulty, forceRefresh: Boolean): List<Question> {
        return withContext(Dispatchers.IO) {
            if (!forceRefresh && cachedQuestions != null && lastCategory == category && lastDifficulty == difficulty) {
                return@withContext cachedQuestions!!
            }

            val categoryId = getCategoryId(category)
            val diffString = difficulty.name.lowercase()
            val response = openTdbApi.getQuestions(
                amount = 7,
                category = categoryId,
                difficulty = diffString
            )
            
            val questions = response.results.map { dto ->
                Question(
                    type = dto.type,
                    difficulty = try { Difficulty.valueOf(dto.difficulty.uppercase()) } catch (e: Exception) { Difficulty.EASY },
                    category = dto.category,
                    question = decodeHtmlEntities(dto.question),
                    correctAnswer = decodeHtmlEntities(dto.correctAnswer),
                    incorrectAnswers = dto.incorrectAnswers.map { decodeHtmlEntities(it) }
                )
            }
            
            lastCategory = category
            lastDifficulty = difficulty
            cachedQuestions = questions
            
            questions
        }
    }

    override fun getScores(): Flow<List<QuizScore>> {
        return scoreDao.getAllScores().map { entities ->
            entities.map { entity ->
                QuizScore(
                    category = entity.category,
                    difficulty = entity.difficulty,
                    score = entity.score,
                    totalQuestions = entity.totalQuestions,
                    date = entity.date
                )
            }
        }
    }

    override suspend fun saveScore(score: QuizScore) {
        withContext(Dispatchers.IO) {
            scoreDao.insertScore(
                ScoreEntity(
                    category = score.category,
                    difficulty = score.difficulty,
                    score = score.score,
                    totalQuestions = score.totalQuestions,
                    date = score.date
                )
            )
        }
    }

    private fun getCategoryId(categoryName: String): Int {
        return when (categoryName) {
            "Sports" -> 21
            "Science & Nature" -> 17
            "Animals" -> 27
            "Geography" -> 22
            "History" -> 23
            "General Knowledge" -> 9
            else -> 9
        }
    }

    private fun decodeHtmlEntities(text: String): String {
        return Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString()
    }
}