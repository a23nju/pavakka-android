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

    // Dashboard
    @GET("dashboard")
    suspend fun getDashboard(@Query("date") date: String): DashboardSummary

    // Diary + food
    @GET("diary")
    suspend fun getDiary(@Query("date") date: String): List<DiaryEntry>

    @GET("foods/search")
    suspend fun searchFoods(@Query("q") query: String): List<FoodItem>

    @POST("diary/entries")
    suspend fun logFood(@Body body: LogFoodRequest): DiaryEntry

    @DELETE("diary/entries")
    suspend fun deleteEntry(@Query("id") id: String): OkResponse

    // Water
    @POST("water")
    suspend fun changeWater(@Body body: WaterRequest): WaterResponse

    // Progress
    @GET("progress/weight")
    suspend fun getWeightLogs(): List<WeightLog>

    @POST("progress/weight")
    suspend fun logWeight(@Body body: WeightRequest): WeightLog

    // Exercise / workout
    @POST("exercise")
    suspend fun logExercise(@Body body: ExerciseRequest): ExerciseLog

    // Fasting
    @GET("fasting")
    suspend fun getFasting(): FastingState

    @POST("fasting")
    suspend fun startFast(@Body body: FastingRequest): FastingSession

    @DELETE("fasting")
    suspend fun endFast(): OkResponse

    // Goals
    @GET("goals")
    suspend fun getGoals(): Goals

    @PUT("goals")
    suspend fun saveGoals(@Body body: Goals): Goals

    // Personal foods
    @GET("foods")
    suspend fun getFoods(): List<FoodItem>

    @POST("foods")
    suspend fun createFood(@Body body: CreateFoodRequest): FoodItem

    @DELETE("foods")
    suspend fun deleteFood(@Query("id") id: String): OkResponse

    // AI meal plan
    @POST("plan")
    suspend fun generatePlan(@Body body: Map<String, String>): MealPlan

    // AI meal scan (photo or text)
    @POST("photo")
    suspend fun scan(@Body body: Map<String, String>): ScanResponse

    // Weekly report
    @GET("report")
    suspend fun getReport(): WeekReport
}

object NetworkService {
    // Replace with your Vercel URL once deployed
    private const val BASE_URL = "https://pavakka-a23njus-projects.vercel.app/api/"

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
