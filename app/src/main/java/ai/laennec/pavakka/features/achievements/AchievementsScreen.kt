package ai.laennec.pavakka.features.achievements

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.laennec.pavakka.core.models.Badge
import ai.laennec.pavakka.core.services.NetworkService
import ai.laennec.pavakka.core.ui.theme.BrandGreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AchievementsViewModel : ViewModel() {
    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak
    private val _badges = MutableStateFlow<List<Badge>>(emptyList())
    val badges: StateFlow<List<Badge>> = _badges

    init { load() }
    fun load() {
        viewModelScope.launch {
            try {
                val r = NetworkService.api.getStreak()
                _streak.value = r.streak; _badges.value = r.badges
            } catch (_: Exception) {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(vm: AchievementsViewModel = viewModel()) {
    val streak by vm.streak.collectAsState()
    val badges by vm.badges.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Achievements") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = BrandGreen.copy(alpha = 0.1f))) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🔥 $streak", fontSize = 40.sp, fontWeight = FontWeight.Bold)
                    Text("day streak", color = Color.Gray)
                }
            }
            Spacer(Modifier.height(16.dp))
            LazyVerticalGrid(columns = GridCells.Fixed(2), verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(badges) { badge ->
                    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.alpha(if (badge.earned) 1f else 0.4f)) {
                        Column(modifier = Modifier.fillMaxWidth().padding(12.dp).heightIn(min = 120.dp),
                            horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(badge.icon, fontSize = 32.sp)
                            Text(badge.title, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
                            Text(badge.desc, fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
                            if (badge.earned) Text("Earned ✓", fontSize = 11.sp, color = BrandGreen)
                        }
                    }
                }
            }
        }
    }
}
