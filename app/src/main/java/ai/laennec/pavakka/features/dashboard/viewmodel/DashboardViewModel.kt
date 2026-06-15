package ai.laennec.pavakka.features.dashboard.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class DashboardViewModel : ViewModel() {
    private val _caloriesEaten = MutableStateFlow(0)
    val caloriesEaten: StateFlow<Int> = _caloriesEaten

    private val _calorieGoal = MutableStateFlow(2000)
    val calorieGoal: StateFlow<Int> = _calorieGoal

    private val _caloriesBurned = MutableStateFlow(0)
    val caloriesBurned: StateFlow<Int> = _caloriesBurned

    private val _waterGlasses = MutableStateFlow(0)
    val waterGlasses: StateFlow<Int> = _waterGlasses

    fun addWater() { if (_waterGlasses.value < 8) _waterGlasses.value++ }
    fun removeWater() { if (_waterGlasses.value > 0) _waterGlasses.value-- }
}
