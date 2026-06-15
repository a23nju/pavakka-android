package ai.laennec.pavakka.features.plan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.laennec.pavakka.core.models.MealPlan
import ai.laennec.pavakka.core.services.NetworkService
import ai.laennec.pavakka.core.ui.theme.BrandGreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlanViewModel : ViewModel() {
    private val _plan = MutableStateFlow<MealPlan?>(null)
    val plan: StateFlow<MealPlan?> = _plan
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun generate() {
        viewModelScope.launch {
            _loading.value = true; _error.value = null
            try { _plan.value = NetworkService.api.generatePlan(emptyMap()) }
            catch (_: Exception) { _error.value = "Couldn't reach the meal planner." }
            _loading.value = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanScreen(vm: PlanViewModel = viewModel()) {
    val plan by vm.plan.collectAsState()
    val loading by vm.loading.collectAsState()
    val error by vm.error.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("AI Meal Plan") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Get a plan for the rest of today that fits your remaining calories.",
                color = Color.Gray, fontSize = 14.sp)
            Button(onClick = { vm.generate() }, enabled = !loading,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)) {
                if (loading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                else Text("Generate Plan")
            }
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp) }
            plan?.let { p ->
                Text("≈ ${p.remaining} kcal to plan", fontSize = 12.sp, color = Color.Gray)
                p.meals.forEach { meal ->
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(meal.meal.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.SemiBold)
                            meal.items.forEach { item ->
                                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                                    Text(item.name, modifier = Modifier.weight(1f), fontSize = 14.sp)
                                    Text("${item.calories} kcal", color = Color.Gray, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
                if (p.note.isNotEmpty()) Text(p.note, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
