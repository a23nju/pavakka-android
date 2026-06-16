package ai.laennec.pavakka.features.more

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ai.laennec.pavakka.features.auth.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoreScreen(authViewModel: AuthViewModel, onNavigate: (String) -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("More") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            row("Fasting", Icons.Filled.Refresh) { onNavigate("fasting") }
            row("Goals", Icons.Filled.Star) { onNavigate("goals") }
            row("My Foods", Icons.Filled.ShoppingCart) { onNavigate("foods") }
            rowEmoji("Reminders", "🔔") { onNavigate("reminders") }
            row("Set Up Profile", Icons.Filled.Person) { onNavigate("onboarding") }
            Divider()
            rowEmoji("AI Coach", "💬") { onNavigate("coach") }
            row("AI Meal Plan", Icons.Filled.Create) { onNavigate("plan") }
            row("Meal Scan", Icons.Filled.Search) { onNavigate("scan") }
            row("Log Exercise (AI)", Icons.Filled.PlayArrow) { onNavigate("exerciseAi") }
            Divider()
            row("Achievements", Icons.Filled.Favorite) { onNavigate("achievements") }
            row("Weekly Report", Icons.Filled.DateRange) { onNavigate("report") }
            Divider()
            row("Log Out", Icons.Filled.ExitToApp) { authViewModel.logout() }
        }
    }
}

@Composable
private fun row(label: String, icon: ImageVector, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = { Icon(icon, contentDescription = label) },
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
private fun rowEmoji(label: String, emoji: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(label) },
        leadingContent = { Text(emoji) },
        modifier = Modifier.clickable { onClick() }
    )
}
