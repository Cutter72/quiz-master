package pl.pdgroup.quiz.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.QuizScore

class GetScoreStatisticsUseCaseTest {

    private lateinit var getScoreStatisticsUseCase: GetScoreStatisticsUseCase

    @Before
    fun setUp() {
        getScoreStatisticsUseCase = GetScoreStatisticsUseCase()
    }

    @Test
    fun `invoke with empty list returns zero statistics`() {
        val scores = emptyList<QuizScore>()
        
        val result = getScoreStatisticsUseCase(scores)
        
        assertEquals(0, result.totalQuizzes)
        assertEquals(0.0, result.averageScore, 0.0)
        assertEquals(0, result.bestScore)
    }

    @Test
    fun `invoke with valid scores calculates statistics correctly`() {
        val scores = listOf(
            QuizScore("Science", Difficulty.EASY, 5, 10, "2023-10-01T10:00:00Z"),
            QuizScore("Math", Difficulty.MEDIUM, 8, 10, "2023-10-02T10:00:00Z"),
            QuizScore("History", Difficulty.HARD, 9, 10, "2023-10-03T10:00:00Z")
        )
        
        val result = getScoreStatisticsUseCase(scores)
        
        // Average: (0.5 + 0.8 + 0.9) / 3 = 2.2 / 3 = 0.7333333333333334
        // Average score percentage: 73.33333333333333
        // Best score: 90
        
        assertEquals(3, result.totalQuizzes)
        assertEquals(73.33333333333333, result.averageScore, 0.0001)
        assertEquals(90, result.bestScore)
    }

    @Test
    fun `invoke with single score calculates statistics correctly`() {
        val scores = listOf(
            QuizScore("Science", Difficulty.EASY, 7, 10, "2023-10-01T10:00:00Z")
        )
        
        val result = getScoreStatisticsUseCase(scores)
        
        assertEquals(1, result.totalQuizzes)
        assertEquals(70.0, result.averageScore, 0.0)
        assertEquals(70, result.bestScore)
    }
}
