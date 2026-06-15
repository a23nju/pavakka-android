package ai.laennec.pavakka.features.goals

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.laennec.pavakka.core.models.Goals
import ai.laennec.pavakka.core.services.NetworkService
import ai.laennec.pavakka.core.ui.theme.BrandGreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GoalsViewModel : ViewModel() {
    private val _goals = MutableStateFlow(Goals(2000, 100, 250, 67))
    val goals: StateFlow<Goals> = _goals
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    init { load() }

    fun load() {
        viewModelScope.launch {
            try { _goals.value = NetworkService.api.getGoals() } catch (_: Exception) {}
        }
    }

    fun save(calories: Int, protein: Int, carbs: Int, fat: Int) {
        viewModelScope.launch {
            try {
                _goals.value = NetworkService.api.saveGoals(Goals(calories, protein, carbs, fat))
                _message.value = "Goals saved ✓"
            } catch (_: Exception) { _message.value = "Couldn't save." }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(vm: GoalsViewModel = viewModel()) {
    val goals by vm.goals.collectAsState()
    val message by vm.message.collectAsState()

    var calories by remember(goals) { mutableStateOf(goals.calories.toString()) }
    var protein by remember(goals) { mutableStateOf(goals.protein.toString()) }
    var carbs by remember(goals) { mutableStateOf(goals.carbs.toString()) }
    var fat by remember(goals) { mutableStateOf(goals.fat.toString()) }

    Scaffold(topBar = { TopAppBar(title = { Text("Goals") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Daily Targets", fontWeight = FontWeight.SemiBold)
            field("Calories (kcal)", calories) { calories = it }
            field("Protein (g)", protein) { protein = it }
            field("Carbs (g)", carbs) { carbs = it }
            field("Fat (g)", fat) { fat = it }
            Button(
                onClick = {
                    vm.save(calories.toIntOrNull() ?: 2000, protein.toIntOrNull() ?: 100,
                        carbs.toIntOrNull() ?: 250, fat.toIntOrNull() ?: 67)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)
            ) { Text("Save Goals") }
            message?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

@Composable
private fun field(label: String, value: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) },
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true, modifier = Modifier.fillMaxWidth()
    )
}
