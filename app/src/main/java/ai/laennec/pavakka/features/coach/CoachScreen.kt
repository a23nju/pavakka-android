package ai.laennec.pavakka.features.coach

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.laennec.pavakka.core.models.CoachRequest
import ai.laennec.pavakka.core.models.CoachTurn
import ai.laennec.pavakka.core.services.NetworkService
import ai.laennec.pavakka.core.ui.theme.BrandGreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CoachViewModel : ViewModel() {
    private val _messages = MutableStateFlow(
        listOf(CoachTurn("model", "Hi! I'm your Pavakka coach. Ask me anything about food, calories, or workouts. 🥗"))
    )
    val messages: StateFlow<List<CoachTurn>> = _messages
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun send(text: String) {
        val t = text.trim()
        if (t.isEmpty()) return
        _messages.value = _messages.value + CoachTurn("user", t)
        viewModelScope.launch {
            _loading.value = true
            try {
                val r = NetworkService.api.coach(CoachRequest(_messages.value, t))
                _messages.value = _messages.value + CoachTurn("model", r.reply)
            } catch (_: Exception) {
                _messages.value = _messages.value + CoachTurn("model", "Sorry, I couldn't reply just now.")
            }
            _loading.value = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoachScreen(vm: CoachViewModel = viewModel()) {
    val messages by vm.messages.collectAsState()
    val loading by vm.loading.collectAsState()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) { if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1) }

    Scaffold(topBar = { TopAppBar(title = { Text("AI Coach") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxWidth().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(messages) { msg ->
                    val isUser = msg.role == "user"
                    Row(modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start) {
                        Surface(color = if (isUser) BrandGreen else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(14.dp)) {
                            Text(msg.text, modifier = Modifier.padding(10.dp).widthIn(max = 260.dp),
                                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
                if (loading) item { Text("Thinking…", color = Color.Gray, fontWeight = FontWeight.Light) }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(input, { input = it }, modifier = Modifier.weight(1f),
                    placeholder = { Text("Ask your coach…") }, maxLines = 3)
                Spacer(Modifier.width(8.dp))
                Button(onClick = { val t = input; input = ""; vm.send(t) },
                    enabled = input.isNotBlank() && !loading,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandGreen)) {
                    Text("Send")
                }
            }
        }
    }
}
