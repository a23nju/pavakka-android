package ai.laennec.pavakka.features.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.laennec.pavakka.core.models.WaterRequest
import ai.laennec.pavakka.core.services.NetworkService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardViewModel : ViewModel() {
    private val _caloriesEaten = MutableStateFlow(0)
    val caloriesEaten: StateFlow<Int> = _caloriesEaten

    private val _calorieGoal = MutableStateFlow(2000)
    val calorieGoal: StateFlow<Int> = _calorieGoal

    private val _caloriesBurned = MutableStateFlow(0)
    val caloriesBurned: StateFlow<Int> = _caloriesBurned

    private val _waterGlasses = MutableStateFlow(0)
    val waterGlasses: StateFlow<Int> = _waterGlasses

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak

    private val _weeklyBalance = MutableStateFlow(0)
    val weeklyBalance: StateFlow<Int> = _weeklyBalance

    private val _todayTarget = MutableStateFlow(0)
    val todayTarget: StateFlow<Int> = _todayTarget

    private val today: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    init { load() }

    fun load() {
        viewModelScope.launch {
            try {
                val s = NetworkService.api.getDashboard(today)
                _caloriesEaten.value = s.caloriesEaten
                _caloriesBurned.value = s.caloriesBurned
                _calorieGoal.value = s.calorieGoal
                _waterGlasses.value = s.waterGlasses
            } catch (_: Exception) {}
            try { _streak.value = NetworkService.api.getStreak().streak } catch (_: Exception) {}
            try {
                val b = NetworkService.api.getCalorieBank()
                _weeklyBalance.value = b.weeklyBalance
                _todayTarget.value = b.todayTarget
            } catch (_: Exception) {}
        }
    }

    fun addWater() = changeWater(1)
    fun removeWater() = changeWater(-1)

    private fun changeWater(delta: Int) {
        viewModelScope.launch {
            try {
                val r = NetworkService.api.changeWater(WaterRequest(today, delta))
                _waterGlasses.value = r.glasses
            } catch (_: Exception) {}
        }
    }
}
