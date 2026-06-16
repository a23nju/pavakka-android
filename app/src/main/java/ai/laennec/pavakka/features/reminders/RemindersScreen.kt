package ai.laennec.pavakka.features.reminders

import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ai.laennec.pavakka.core.notifications.Reminders
import ai.laennec.pavakka.core.ui.theme.BrandGreen

// Simple SharedPreferences-backed reminder settings (no backend).
private fun prefs(c: Context) = c.getSharedPreferences("pavakka_reminders", Context.MODE_PRIVATE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersScreen() {
    val context = LocalContext.current
    val sp = remember { prefs(context) }

    var waterOn by remember { mutableStateOf(sp.getBoolean("water", false)) }
    var waterInterval by remember { mutableStateOf(sp.getInt("water_int", 2)) }
    var mealsOn by remember { mutableStateOf(sp.getBoolean("meals", false)) }
    var pendingEnable by remember { mutableStateOf<(() -> Unit)?>(null) }

    val permLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) pendingEnable?.invoke()
        pendingEnable = null
    }

    // Run [action] after ensuring notification permission (Android 13+).
    fun withPermission(action: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pendingEnable = action
            permLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        } else action()
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Reminders") }) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Gentle nudges so you never forget to log or hydrate. All reminders stay on your device.",
                fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))

            // Water reminders
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text("💧 Water reminders", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Switch(checked = waterOn, onCheckedChange = { on ->
                            val apply = {
                                waterOn = on; sp.edit().putBoolean("water", on).apply()
                                if (on) Reminders.scheduleWater(context, waterInterval) else Reminders.cancelWater(context)
                            }
                            if (on) withPermission(apply) else apply()
                        })
                    }
                    if (waterOn) {
                        Spacer(Modifier.height(8.dp))
                        Text("Remind me every", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(1, 2, 3, 4).forEach { h ->
                                FilterChip(
                                    selected = waterInterval == h,
                                    onClick = {
                                        waterInterval = h; sp.edit().putInt("water_int", h).apply()
                                        Reminders.scheduleWater(context, h)
                                    },
                                    label = { Text("${h}h") },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = BrandGreen)
                                )
                            }
                        }
                    }
                }
            }

            // Meal reminders
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text("🍽️ Meal logging reminders", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Switch(checked = mealsOn, onCheckedChange = { on ->
                            val apply = {
                                mealsOn = on; sp.edit().putBoolean("meals", on).apply()
                                if (on) Reminders.scheduleMeals(context) else Reminders.cancelMeals(context)
                            }
                            if (on) withPermission(apply) else apply()
                        })
                    }
                    if (mealsOn) {
                        Spacer(Modifier.height(4.dp))
                        Text("Breakfast 9:00 · Lunch 13:30 · Dinner 20:00",
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
