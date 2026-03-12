package pl.pdgroup.quiz.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pl.pdgroup.quiz.data.local.QuizDatabase
import pl.pdgroup.quiz.data.local.ScoreDao
import pl.pdgroup.quiz.data.repository.QuizRepositoryImpl
import pl.pdgroup.quiz.domain.repository.QuizRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideQuizDatabase(app: Application): QuizDatabase {
        return Room.databaseBuilder(
            app,
            QuizDatabase::class.java,
            "quiz_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideScoreDao(db: QuizDatabase): ScoreDao {
        return db.scoreDao
    }

    @Provides
    @Singleton
    fun provideQuizRepository(
        app: Application,
        scoreDao: ScoreDao
    ): QuizRepository {
        return QuizRepositoryImpl(app, scoreDao)
    }
}