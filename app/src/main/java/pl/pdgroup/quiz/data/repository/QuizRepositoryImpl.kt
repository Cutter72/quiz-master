package pl.pdgroup.quiz.data.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import pl.pdgroup.quiz.data.local.ScoreDao
import pl.pdgroup.quiz.data.local.ScoreEntity
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.Question
import pl.pdgroup.quiz.domain.model.QuizScore
import pl.pdgroup.quiz.domain.repository.QuizRepository

class QuizRepositoryImpl(
    private val context: Context,
    private val scoreDao: ScoreDao
) : QuizRepository {

    private val jsonParser = Json { ignoreUnknownKeys = true }
    private var cachedQuestions: List<Question>? = null

    override suspend fun getQuestions(category: String, difficulty: Difficulty): List<Question> {
        return withContext(Dispatchers.IO) {
            val allQuestions = loadQuestions()
            allQuestions.filter { it.category == category && it.difficulty == difficulty }
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

    private fun loadQuestions(): List<Question> {
        if (cachedQuestions != null) return cachedQuestions!!
        
        return try {
            val jsonString = context.assets.open("questions.json").bufferedReader().use { it.readText() }
            val parsed = jsonParser.decodeFromString<List<Question>>(jsonString)
            cachedQuestions = parsed
            parsed
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}