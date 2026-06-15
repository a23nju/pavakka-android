package ai.laennec.pavakka.core.models

data class User(
    val id: String,
    val name: String?,
    val email: String,
    // Auth endpoints return only id/name/email, so the rest are nullable.
    val goalCalories: Int? = null,
    val goalProtein: Int? = null,
    val goalCarbs: Int? = null,
    val goalFat: Int? = null,
    val weightKg: Double? = null,
    val goalWeightKg: Double? = null
)

data class LoginRequest(val email: String, val password: String)
data class SignUpRequest(val name: String, val email: String, val password: String)
data class LoginResponse(val token: String, val user: User)

data class DiaryEntry(
    val id: String,
    val foodName: String,
    val meal: String,
    val quantity: Double,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double
)

data class FoodItem(
    val id: String?,           // null for Open Food Facts search results
    val name: String,
    val brand: String?,
    val barcode: String? = null,
    val calories: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val servingSize: Double,
    val servingUnit: String,
    val source: String
)

data class WeightLog(
    val id: String,
    val date: String,
    val weightKg: Double
)

// --- Dashboard ---
data class DashboardSummary(
    val caloriesEaten: Int,
    val caloriesBurned: Int,
    val calorieGoal: Int,
    val protein: Double,
    val carbs: Double,
    val fat: Double,
    val proteinGoal: Int,
    val carbsGoal: Int,
    val fatGoal: Int,
    val waterGlasses: Int
)

// --- Requests / small responses ---
data class OkResponse(val ok: Boolean? = null)
data class WaterRequest(val date: String, val delta: Int)
data class WaterResponse(val glasses: Int)
data class WeightRequest(val date: String, val weightKg: Double)

data class LoggedFoodPayload(
    val name: String, val brand: String?, val barcode: String?,
    val servingSize: Double, val servingUnit: String,
    val calories: Int, val protein: Double, val carbs: Double, val fat: Double,
    val source: String
)
// Either foodId+quantity (personal food) OR food+grams (search/ad-hoc).
data class LogFoodRequest(
    val foodId: String? = null,
    val quantity: Double? = null,
    val food: LoggedFoodPayload? = null,
    val grams: Double? = null,
    val meal: String,
    val date: String
)

// --- Exercise ---
data class ExerciseRequest(val name: String, val caloriesBurned: Int, val minutes: Double, val date: String)
data class ExerciseLog(val id: String, val name: String, val minutes: Double?, val caloriesBurned: Int, val date: String)

// --- Fasting ---
data class FastingRequest(val targetHours: Int)
data class FastingSession(val id: String, val startedAt: String, val endedAt: String?, val targetHours: Int)
data class FastingState(val active: FastingSession?, val history: List<FastingSession>)

// --- Goals ---
data class Goals(val calories: Int, val protein: Int, val carbs: Int, val fat: Int)

// --- Personal foods ---
data class CreateFoodRequest(
    val name: String, val brand: String?, val servingSize: Double, val servingUnit: String,
    val calories: Int, val protein: Double, val carbs: Double, val fat: Double
)

// --- AI meal plan ---
data class PlannedItem(val name: String, val calories: Int, val protein: Int, val carbs: Int, val fat: Int)
data class PlannedMeal(val meal: String, val items: List<PlannedItem>)
data class MealPlan(val meals: List<PlannedMeal>, val note: String, val remaining: Int)

// --- AI meal scan ---
data class ScannedItem(val name: String, val calories: Int, val protein: Int, val carbs: Int, val fat: Int)
data class ScanResponse(val items: List<ScannedItem>, val note: String)

// --- Weekly report ---
data class DailyCalories(val date: String, val calories: Int)
data class WeekReport(
    val daysLogged: Int,
    val avgCalories: Int,
    val goalCalories: Int,
    val caloriesBurned: Int,
    val weightChangeKg: Double?,
    val dailyCalories: List<DailyCalories>,
    val summary: String
)
