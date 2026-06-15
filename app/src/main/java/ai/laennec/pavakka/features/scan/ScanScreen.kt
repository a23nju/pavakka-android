package ai.laennec.pavakka.features.scan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.laennec.pavakka.core.models.*
import ai.laennec.pavakka.core.services.NetworkService
import ai.laennec.pavakka.core.ui.theme.BrandGreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ScanViewModel : ViewModel() {
    private val _items = MutableStateFlow<List<ScannedItem>>(emptyList())
    val items: StateFlow<List<ScannedItem>> = _items
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error
    private val _logged = MutableStateFlow(false)
    val logged: StateFlow<Boolean> = _logged

    private val today: String get() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    fun scanText(text: String) {
        viewModelScope.launch {
            _loading.value = true; _error.value = null; _items.value = emptyList(); _logged.value = false
            try {
                val r = NetworkService.api.scan(mapOf("text" to text))
                _items.value = r.items
                if (r.items.isEmpty()) _error.value = r.note.ifEmpty { "No food found." }
            } catch (_: Exception) { _error.value = "Couldn't reach the meal scanner." }
            _loading.value = false
        }
    }

    fun logAll(meal: String) {
        viewModelScope.launch {
            for (item in _items.value) {
                val payload = LoggedFoodPayload(item.name, null, null, 100.0, "serving",
                    item.calories, item.protein.toDouble(), item.carbs.toDouble(), item.fat.toDouble(), "ai")
                try {
                    NetworkService.api.logFood(LogFoodRequest(food = payload, grams = 100.0, meal = meal, date = today))
                } catch (_: Exception) {}
            }
            _logged.value = true
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(vm: ScanViewModel = viewModel()) {
    val items by vm.items.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()
    val logged by vm.logged.collectAsState()
    var text by remember { mutableStateOf("") }
    var meal by remember { mutableStateOf("lunch") }
    val meals = listOf("breakfast", "lunch", "dinner", "snack")

    Scaffold(topBar = { TopAppBar(title = { Text("Meal Scan") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Describe your meal — AI estimates the nutrition.", color = Color.Gray, fontSize = 14.sp)
            OutlinedTextField(text, { text = it }, label = { Text("e.g. 2 idli, sambar, coffee") },
                modifier = Modifier.fillMaxWidth())
            Button(onClick = { vm.scanText(text) }, enabled = text.isNotBlank() && !loading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)) {
                if (loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                else Text("Scan")
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }

            if (items.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        items.forEach { item ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                                Text(item.name, modifier = Modifier.weight(1f), fontSize = 14.sp)
                                Text("${item.calories} kcal", color = Color.Gray, fontSize = 14.sp)
                            }
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    meals.forEach { m ->
                        FilterChip(selected = meal == m, onClick = { meal = m },
                            label = { Text(m.replaceFirstChar { it.uppercase() }, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BrandGreen))
                    }
                }
                Button(onClick = { vm.logAll(meal) }, enabled = !logged,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = if (logged) Color.Gray else BrandGreen)) {
                    Text(if (logged) "Logged ✓" else "Log all to ${meal.replaceFirstChar { it.uppercase() }}")
                }
            }
        }
    }
}
