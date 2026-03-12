package pl.pdgroup.quiz.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ScoreEntity::class], version = 1, exportSchema = false)
abstract class QuizDatabase : RoomDatabase() {
    abstract val scoreDao: ScoreDao
}