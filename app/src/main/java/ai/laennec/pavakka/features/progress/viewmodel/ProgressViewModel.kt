package ai.laennec.pavakka.features.progress.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.laennec.pavakka.core.models.WeightLog
import ai.laennec.pavakka.core.models.WeightRequest
import ai.laennec.pavakka.core.services.NetworkService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProgressViewModel : ViewModel() {
    private val _weightLogs = MutableStateFlow<List<WeightLog>>(emptyList())
    val weightLogs: StateFlow<List<WeightLog>> = _weightLogs

    private val today: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    init { load() }

    fun load() {
        viewModelScope.launch {
            try { _weightLogs.value = NetworkService.api.getWeightLogs() } catch (_: Exception) {}
        }
    }

    fun logWeight(weight: Float) {
        viewModelScope.launch {
            try {
                NetworkService.api.logWeight(WeightRequest(today, weight.toDouble()))
                load()
            } catch (_: Exception) {}
        }
    }
}
