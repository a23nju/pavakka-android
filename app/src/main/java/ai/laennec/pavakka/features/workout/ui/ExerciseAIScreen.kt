package ai.laennec.pavakka.features.workout.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.laennec.pavakka.core.models.ExerciseEstimate
import ai.laennec.pavakka.core.models.ExerciseRequest
import ai.laennec.pavakka.core.services.NetworkService
import ai.laennec.pavakka.core.ui.theme.BrandGreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExerciseAIViewModel : ViewModel() {
    private val _items = MutableStateFlow<List<ExerciseEstimate>>(emptyList())
    val items: StateFlow<List<ExerciseEstimate>> = _items
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    private val _logged = MutableStateFlow(false)
    val logged: StateFlow<Boolean> = _logged

    private val today get() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun estimate(text: String) {
        viewModelScope.launch {
            _loading.value = true; _error.value = null; _items.value = emptyList(); _logged.value = false
            try {
                val r = NetworkService.api.estimateExercise(mapOf("text" to text))
                _items.value = r.items
                if (r.items.isEmpty()) _error.value = r.note.ifEmpty { "No activity found." }
            } catch (_: Exception) { _error.value = "Couldn't reach the estimator." }
            _loading.value = false
        }
    }

    fun logAll() {
        viewModelScope.launch {
            for (item in _items.value) {
                try { NetworkService.api.logExercise(ExerciseRequest(item.name, item.caloriesBurned, item.minutes.toDouble(), today)) }
                catch (_: Exception) {}
            }
            _logged.value = true
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseAIScreen(vm: ExerciseAIViewModel = viewModel()) {
    val items by vm.items.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    val logged by vm.logged.collectAsState()
    var text by remember { mutableStateOf("") }

    Scaffold(topBar = { TopAppBar(title = { Text("Log Exercise (AI)") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Describe what you did — AI estimates calories burned for your body weight.",
                color = Color.Gray, fontSize = 14.sp)
            OutlinedTextField(text, { text = it }, label = { Text("e.g. walked 30 min, 20 pushups") },
                modifier = Modifier.fillMaxWidth())
            Button(onClick = { vm.estimate(text) }, enabled = text.isNotBlank() && !loading,
                modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)) {
                if (loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp)) else Text("Estimate")
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
            if (items.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        items.forEach { item ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                Text("${item.name} (${item.minutes} min)", modifier = Modifier.weight(1f), fontSize = 14.sp)
                                Text("${item.caloriesBurned} kcal", color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                    }
                }
                Button(onClick = { vm.logAll() }, enabled = !logged, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = if (logged) Color.Gray else BrandGreen)) {
                    Text(if (logged) "Logged ✓" else "Log all")
                }
            }
        }
    }
}
