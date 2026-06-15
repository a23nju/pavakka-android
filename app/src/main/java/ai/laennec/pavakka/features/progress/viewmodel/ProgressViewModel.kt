package ai.laennec.pavakka.features.progress.viewmodel

import androidx.lifecycle.ViewModel
import ai.laennec.pavakka.core.models.WeightLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProgressViewModel : ViewModel() {
    private val _weightLogs = MutableStateFlow<List<WeightLog>>(emptyList())
    val weightLogs: StateFlow<List<WeightLog>> = _weightLogs

    fun logWeight(weight: Float) {
        val date = SimpleDateFormat("MMM d", Locale.getDefault()).format(Date())
        val id = System.currentTimeMillis().toString()
        _weightLogs.value = _weightLogs.value + WeightLog(id = id, date = date, weightKg = weight.toDouble())
    }
}
