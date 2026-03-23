package pl.pdgroup.quiz.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.Question
import pl.pdgroup.quiz.domain.model.QuizScore
import pl.pdgroup.quiz.domain.repository.QuizRepository

class SaveScoreUseCaseTest {

    private lateinit var saveScoreUseCase: SaveScoreUseCase
    private lateinit var fakeRepository: FakeQuizRepository

    @Before
    fun setUp() {
        fakeRepository = FakeQuizRepository()
        saveScoreUseCase = SaveScoreUseCase(fakeRepository)
    }

    @Test
    fun `invoke saves correct score to repository`() = runBlocking {
        // Given
        val category = "Science"
        val difficulty = Difficulty.HARD
        val score = 8
        val totalQuestions = 10

        // When
        saveScoreUseCase(category, difficulty, score, totalQuestions)

        // Then
        assertEquals(1, fakeRepository.savedScores.size)
        val savedScore = fakeRepository.savedScores.first()
        
        assertEquals(category, savedScore.category)
        assertEquals(difficulty, savedScore.difficulty)
        assertEquals(score, savedScore.score)
        assertEquals(totalQuestions, savedScore.totalQuestions)
        // We do not strictly test the `date` string generated internally, 
        // but verify it's not empty
        assert(savedScore.date.isNotEmpty())
    }
}
