package pl.pdgroup.quiz.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDao {
    @Query("SELECT * FROM quiz_scores ORDER BY date DESC")
    fun getAllScores(): Flow<List<ScoreEntity>>

    @Insert
    suspend fun insertScore(score: ScoreEntity)
}