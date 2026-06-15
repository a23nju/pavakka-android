package ai.laennec.pavakka.core.ui

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.laennec.pavakka.core.services.AuthPreferences
import ai.laennec.pavakka.features.auth.ui.LoginScreen
import ai.laennec.pavakka.features.auth.viewmodel.AuthViewModel

@Composable
fun RootScreen(authViewModel: AuthViewModel = viewModel()) {
    val isLoggedIn by authViewModel.isLoggedIn.collectAsState()

    if (isLoggedIn) {
        MainNavHost(authViewModel = authViewModel)
    } else {
        LoginScreen(authViewModel = authViewModel)
    }
}
