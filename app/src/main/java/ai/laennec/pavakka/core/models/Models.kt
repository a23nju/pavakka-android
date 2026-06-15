package ai.laennec.pavakka.core.models

data class User(
    val id: String,
    val name: String,
    val email: String,
    val goalCalories: Int = 2000,
    val goalProtein: Int = 150,
    val goalCarbs: Int = 250,
    val goalFat: Int = 65,
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
    val id: String,
    val name: String,
    val brand: String?,
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
