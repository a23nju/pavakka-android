package ai.laennec.pavakka.features.fasting.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class FastingViewModel : ViewModel() {
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning

    private val _elapsedSeconds = MutableStateFlow(0L)
    val elapsedSeconds: StateFlow<Long> = _elapsedSeconds

    private val _selectedProtocol = MutableStateFlow("16:8")
    val selectedProtocol: StateFlow<String> = _selectedProtocol

    private var timerJob: Job? = null

    fun start() {
        _isRunning.value = true
        timerJob = viewModelScope.launch {
            while (_isRunning.value) {
                delay(1000)
                _elapsedSeconds.value++
            }
        }
    }

    fun stop() {
        _isRunning.value = false
        timerJob?.cancel()
        _elapsedSeconds.value = 0
    }

    fun selectProtocol(protocol: String) {
        if (!_isRunning.value) _selectedProtocol.value = protocol
    }
}
