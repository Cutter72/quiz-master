package pl.pdgroup.quiz.di

import android.app.Application
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import pl.pdgroup.quiz.data.local.QuizDatabase
import pl.pdgroup.quiz.data.local.ScoreDao
import pl.pdgroup.quiz.data.remote.api.OpenTdbApi
import pl.pdgroup.quiz.data.repository.QuizRepositoryImpl
import pl.pdgroup.quiz.domain.repository.QuizRepository
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
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
    fun provideJson(): Json {
        return Json { ignoreUnknownKeys = true }
    }

    @Provides
    @Singleton
    fun provideRetrofit(json: Json): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl("https://opentdb.com/")
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenTdbApi(retrofit: Retrofit): OpenTdbApi {
        return retrofit.create(OpenTdbApi::class.java)
    }

    @Provides
    @Singleton
    fun provideQuizRepository(
        scoreDao: ScoreDao,
        openTdbApi: OpenTdbApi
    ): QuizRepository {
        return QuizRepositoryImpl(scoreDao, openTdbApi)
    }
}