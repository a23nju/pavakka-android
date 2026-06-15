package ai.laennec.pavakka.features.workout.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.laennec.pavakka.core.models.ExerciseRequest
import ai.laennec.pavakka.core.services.NetworkService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

class WorkoutViewModel : ViewModel() {
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds

    private val _selectedExercise = MutableStateFlow("Running")
    val selectedExercise: StateFlow<String> = _selectedExercise

    private val _caloriesBurned = MutableStateFlow(0)
    val caloriesBurned: StateFlow<Int> = _caloriesBurned

    private val metValues = mapOf(
        "Running" to 9.8, "Walking" to 3.5, "Cycling" to 7.5,
        "Swimming" to 8.0, "Yoga" to 3.0, "Weight Training" to 5.0, "HIIT" to 8.0
    )

    private var timerJob: Job? = null

    private val today: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun start() {
        _isRunning.value = true
        timerJob = viewModelScope.launch {
            while (_isRunning.value) {
                delay(1000)
                _elapsedSeconds.value++
                val hours = _elapsedSeconds.value / 3600.0
                val met = metValues[_selectedExercise.value] ?: 5.0
                _caloriesBurned.value = (met * 70 * hours).roundToInt()
            }
        }
    }

    fun stop() {
        _isRunning.value = false
        timerJob?.cancel()
        val burned = _caloriesBurned.value
        val minutes = _elapsedSeconds.value / 60.0
        val name = _selectedExercise.value
        if (burned > 0) {
            viewModelScope.launch {
                try { NetworkService.api.logExercise(ExerciseRequest(name, burned, minutes, today)) }
                catch (_: Exception) {}
            }
        }
        _elapsedSeconds.value = 0
        _caloriesBurned.value = 0
    }

    fun selectExercise(exercise: String) {
        if (!_isRunning.value) _selectedExercise.value = exercise
    }
}
