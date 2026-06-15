package ai.laennec.pavakka.features.report

import androidx.compose.foundation.Canvas
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
import ai.laennec.pavakka.core.models.WeekReport
import ai.laennec.pavakka.core.services.NetworkService
import ai.laennec.pavakka.core.ui.theme.BrandGreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReportViewModel : ViewModel() {
    private val _report = MutableStateFlow<WeekReport?>(null)
    val report: StateFlow<WeekReport?> = _report
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    init { load() }

    fun load() {
        viewModelScope.launch {
            _loading.value = true
            try { _report.value = NetworkService.api.getReport() } catch (_: Exception) {}
            _loading.value = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(vm: ReportViewModel = viewModel()) {
    val report by vm.report.collectAsState()
    val loading by vm.loading.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Weekly Report") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(color = BrandGreen)
            } else report?.let { r ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    stat("Days logged", r.daysLogged.toString())
                    stat("Avg kcal", r.avgCalories.toString())
                    stat("Burned", r.caloriesBurned.toString())
                }
                r.weightChangeKg?.let {
                    Text("Weight change: %+.1f kg".format(it), color = Color.Gray, fontSize = 14.sp)
                }
                // Simple bar chart
                val maxCal = (r.dailyCalories.maxOfOrNull { it.calories } ?: 1).coerceAtLeast(1)
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().height(160.dp).padding(12.dp),
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        r.dailyCalories.forEach { day ->
                            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                Canvas(modifier = Modifier.fillMaxWidth().height((120 * day.calories / maxCal).dp)) {
                                    drawRect(BrandGreen)
                                }
                                Text(day.date.takeLast(2), fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }
                if (r.summary.isNotEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandGreen.copy(alpha = 0.1f))) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Coach's note", fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(4.dp))
                            Text(r.summary, fontSize = 14.sp)
                        }
                    }
                }
            } ?: Text("No data yet — log a few days to see your report.", color = Color.Gray)
        }
    }
}

@Composable
private fun stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(label, fontSize = 12.sp, color = Color.Gray)
    }
}
