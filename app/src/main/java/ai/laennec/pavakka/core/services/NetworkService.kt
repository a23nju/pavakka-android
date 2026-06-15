package ai.laennec.pavakka.core.services

import ai.laennec.pavakka.core.models.*
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface PavakkaApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/signup")
    suspend fun signUp(@Body request: SignUpRequest): LoginResponse

    @GET("diary")
    suspend fun getDiary(@Query("date") date: String): List<DiaryEntry>

    @GET("foods/search")
    suspend fun searchFoods(@Query("q") query: String): List<FoodItem>

    @GET("progress/weight")
    suspend fun getWeightLogs(): List<WeightLog>

    @POST("progress/weight")
    suspend fun logWeight(@Body body: Map<String, Double>): WeightLog
}

object NetworkService {
    // Replace with your Vercel URL once deployed
    private const val BASE_URL = "https://pavakka.vercel.app/api/"

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .addInterceptor { chain ->
            val token = AuthPreferences.token
            val request = if (token != null) {
                chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else chain.request()
            chain.proceed(request)
        }
        .build()

    val api: PavakkaApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PavakkaApi::class.java)
}
