package ai.laennec.pavakka.features.fasting.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.laennec.pavakka.core.models.FastingRequest
import ai.laennec.pavakka.core.services.NetworkService
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class FastingViewModel : ViewModel() {
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds

    // Target fast length in hours — the single source of truth (any value 1–48).
    private val _targetHours = MutableStateFlow(16)
    val targetHours: StateFlow<Int> = _targetHours

    private var timerJob: Job? = null
    private var startMillis: Long = 0L

    private val iso = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    init { load() }

    fun load() {
        viewModelScope.launch {
            try {
                val state = NetworkService.api.getFasting()
                val active = state.active
                if (active != null) {
                    startMillis = parseMillis(active.startedAt)
                    _targetHours.value = active.targetHours
                    _isRunning.value = true
                    runTimer()
                } else {
                    _isRunning.value = false
                    timerJob?.cancel()
                }
            } catch (_: Exception) {}
        }
    }

    fun start() {
        viewModelScope.launch {
            try {
                val s = NetworkService.api.startFast(FastingRequest(_targetHours.value))
                startMillis = parseMillis(s.startedAt)
                _isRunning.value = true
                runTimer()
            } catch (_: Exception) {}
        }
    }

    fun stop() {
        viewModelScope.launch {
            try { NetworkService.api.endFast() } catch (_: Exception) {}
            _isRunning.value = false
            timerJob?.cancel()
            _elapsedSeconds.value = 0
        }
    }

    // Set a target in hours (clamped 1–48); ignored while a fast is running.
    fun setTargetHours(hours: Int) {
        if (!_isRunning.value) _targetHours.value = hours.coerceIn(1, 48)
    }

    private fun runTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isRunning.value) {
                _elapsedSeconds.value = (System.currentTimeMillis() - startMillis) / 1000
                delay(1000)
            }
        }
    }

    private fun parseMillis(s: String): Long = try { iso.parse(s)?.time ?: System.currentTimeMillis() }
        catch (_: Exception) { System.currentTimeMillis() }
}
