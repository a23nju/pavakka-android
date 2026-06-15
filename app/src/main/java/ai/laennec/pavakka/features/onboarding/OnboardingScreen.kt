package ai.laennec.pavakka.features.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.laennec.pavakka.core.models.OnboardingRequest
import ai.laennec.pavakka.core.services.NetworkService
import ai.laennec.pavakka.core.ui.theme.BrandGreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel : ViewModel() {
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun save(name: String, sex: String, age: Int?, height: Double?, weight: Double?, goalWeight: Double?, activity: String, pace: String) {
        if (age == null || height == null || weight == null) { _message.value = "Fill in age, height and weight."; return }
        viewModelScope.launch {
            try {
                val r = NetworkService.api.onboard(OnboardingRequest(name, sex, age, height, weight, goalWeight, activity, pace))
                _message.value = "Saved! Daily goal: ${r.calories ?: "?"} kcal"
            } catch (_: Exception) { _message.value = "Couldn't save your profile." }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(vm: OnboardingViewModel = viewModel()) {
    var name by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf("female") }
    var age by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var goalWeight by remember { mutableStateOf("") }
    var activity by remember { mutableStateOf("moderate") }
    var pace by remember { mutableStateOf("maintain") }
    val message by vm.message.collectAsState()
    val num = KeyboardOptions(keyboardType = KeyboardType.Number)

    val activities = listOf("sedentary", "light", "moderate", "active")
    val paces = listOf("lose_0.5" to "Lose 0.5kg/wk", "lose_0.25" to "Lose 0.25kg/wk",
        "maintain" to "Maintain", "gain_0.25" to "Gain 0.25kg/wk", "gain_0.5" to "Gain 0.5kg/wk")

    Scaffold(topBar = { TopAppBar(title = { Text("Set Up Profile") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(sex == "female", { sex = "female" }, { Text("Female") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BrandGreen))
                FilterChip(sex == "male", { sex = "male" }, { Text("Male") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BrandGreen))
            }
            OutlinedTextField(age, { age = it }, label = { Text("Age") }, keyboardOptions = num, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(height, { height = it }, label = { Text("Height (cm)") }, keyboardOptions = num, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(weight, { weight = it }, label = { Text("Weight (kg)") }, keyboardOptions = num, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(goalWeight, { goalWeight = it }, label = { Text("Goal weight (kg)") }, keyboardOptions = num, singleLine = true, modifier = Modifier.fillMaxWidth())

            Text("Activity level")
            Column {
                activities.forEach { a ->
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(selected = activity == a, onClick = { activity = a })
                        Text(a.replaceFirstChar { it.uppercase() })
                    }
                }
            }
            Text("Goal pace")
            Column {
                paces.forEach { (key, label) ->
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        RadioButton(selected = pace == key, onClick = { pace = key })
                        Text(label)
                    }
                }
            }
            Button(onClick = {
                vm.save(name, sex, age.toIntOrNull(), height.toDoubleOrNull(), weight.toDoubleOrNull(),
                    goalWeight.toDoubleOrNull(), activity, pace)
            }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)) {
                Text("Save & Calculate Goals")
            }
            message?.let { Text(it, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}
