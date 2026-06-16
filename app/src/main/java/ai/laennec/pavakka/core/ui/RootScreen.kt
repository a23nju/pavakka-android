package ai.laennec.pavakka.core.ui

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.laennec.pavakka.features.auth.ui.LoginScreen
import ai.laennec.pavakka.features.auth.viewmodel.AuthViewModel
import ai.laennec.pavakka.features.onboarding.OnboardingScreen

@Composable
fun RootScreen(authViewModel: AuthViewModel = viewModel()) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()
    val isOnboarded by authViewModel.isOnboarded.collectAsState()

    if (!isLoggedIn) {
        LoginScreen(authViewModel = authViewModel)
    } else if (!isOnboarded) {
        // New users complete the height/weight/goal wizard before the dashboard.
        OnboardingScreen(onDone = { authViewModel.markOnboarded() })
    } else {
        MainNavHost(authViewModel = authViewModel)
    }
}
