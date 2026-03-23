package pl.pdgroup.quiz.domain.usecase

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pl.pdgroup.quiz.domain.model.Difficulty
import pl.pdgroup.quiz.domain.model.Question

class GetQuestionsUseCaseTest {

    private lateinit var getQuestionsUseCase: GetQuestionsUseCase
    private lateinit var fakeRepository: FakeQuizRepository

    @Before
    fun setUp() {
        fakeRepository = FakeQuizRepository()
        getQuestionsUseCase = GetQuestionsUseCase(fakeRepository)
    }

    @Test
    fun `invoke returns failure when repository returns less than 7 questions`() = runBlocking<Unit> {
        // Given
        val questions = List(5) { createQuestion("Q$it") }
        fakeRepository.questionsToReturn = questions

        // When
        val result = getQuestionsUseCase("Any Category", Difficulty.EASY)

        // Then
        assertTrue(result.isFailure)
        result.onFailure { error ->
            assertTrue(error.message!!.contains("Not enough questions available"))
        }
    }

    @Test
    fun `invoke returns failure when repository throws exception`() = runBlocking<Unit> {
        // Given
        fakeRepository.shouldThrowException = true

        // When
        val result = getQuestionsUseCase("Any Category", Difficulty.HARD)

        // Then
        assertTrue(result.isFailure)
        result.onFailure { error ->
            assertEquals("Network error", error.message)
        }
    }

    @Test
    fun `invoke returns exact 7 questions when repository returns 7 questions`() = runBlocking<Unit> {
        // Given
        val questions = List(7) { createQuestion("Q$it") }
        fakeRepository.questionsToReturn = questions

        // When
        val result = getQuestionsUseCase("Any Category", Difficulty.MEDIUM)

        // Then
        assertTrue(result.isSuccess)
        val fetchedQuestions = result.getOrNull()
        assertEquals(7, fetchedQuestions?.size)
    }

    @Test
    fun `invoke returns exactly 7 questions when repository returns more than 7 questions`() = runBlocking<Unit> {
        // Given
        val questions = List(15) { createQuestion("Q$it") }
        fakeRepository.questionsToReturn = questions

        // When
        val result = getQuestionsUseCase("Any Category", Difficulty.HARD)

        // Then
        assertTrue(result.isSuccess)
        val fetchedQuestions = result.getOrNull()
        assertEquals(7, fetchedQuestions?.size)
    }

    private fun createQuestion(id: String): Question {
        return Question(
            type = "multiple",
            difficulty = Difficulty.EASY,
            category = "General Knowledge",
            question = "Question $id",
            correctAnswer = "True",
            incorrectAnswers = listOf("False1", "False2", "False3")
        )
    }
}
