package pl.pdgroup.quiz.domain.usecase

import kotlinx.coroutines.flow.Flow
import pl.pdgroup.quiz.domain.model.QuizScore
import pl.pdgroup.quiz.domain.repository.QuizRepository
import javax.inject.Inject

class GetScoresUseCase @Inject constructor(
    private val repository: QuizRepository
) {
    operator fun invoke(): Flow<List<QuizScore>> {
        return repository.getScores()
    }
}