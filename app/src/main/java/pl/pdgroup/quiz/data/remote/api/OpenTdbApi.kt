package pl.pdgroup.quiz.data.remote.api

import pl.pdgroup.quiz.data.remote.model.OpenTdbResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenTdbApi {

    @GET("api.php")
    suspend fun getQuestions(
        @Query("amount") amount: Int,
        @Query("category") category: Int,
        @Query("difficulty") difficulty: String,
        @Query("type") type: String = "multiple"
    ): OpenTdbResponse
}
