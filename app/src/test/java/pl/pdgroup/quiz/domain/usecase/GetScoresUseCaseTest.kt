package pl.pdgroup.quiz.domain.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.QuizScore

class GetScoresUseCaseTest {

    private lateinit var getScoresUseCase: GetScoresUseCase
    private lateinit var fakeRepository: FakeQuizRepository

    @Before
    fun setUp() {
        fakeRepository = FakeQuizRepository()
        getScoresUseCase = GetScoresUseCase(fakeRepository)
    }

    @Test
    fun `invoke returns flow of scores from repository`() = runBlocking {
        // Given
        val scores = listOf(
            QuizScore("Art", Difficulty.EASY, 5, 10, "2023-11-01T12:00:00Z"),
            QuizScore("Sports", Difficulty.HARD, 7, 10, "2023-11-02T12:00:00Z")
        )
        fakeRepository.savedScores.addAll(scores)

        // When
        val resultFlow = getScoresUseCase()
        val resultScores = resultFlow.first()

        // Then
        assertEquals(2, resultScores.size)
        assertEquals(scores, resultScores)
    }
}
