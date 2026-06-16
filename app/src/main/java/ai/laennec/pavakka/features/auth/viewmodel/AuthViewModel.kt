package ai.laennec.pavakka.features.auth.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import ai.laennec.pavakka.core.models.User
import ai.laennec.pavakka.core.services.AuthPreferences
import ai.laennec.pavakka.core.services.NetworkService
import ai.laennec.pavakka.core.models.LoginRequest
import ai.laennec.pavakka.core.models.SignUpRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _isOnboarded = MutableStateFlow(true)
    val isOnboarded: StateFlow<Boolean> = _isOnboarded

    init {
        viewModelScope.launch {
            AuthPreferences.init(application)
            _isLoggedIn.value = AuthPreferences.token != null
            _isOnboarded.value = AuthPreferences.onboarded
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = NetworkService.api.login(LoginRequest(email, password))
                AuthPreferences.saveToken(getApplication(), response.token)
                val onboarded = response.user.onboarded ?: true
                AuthPreferences.setOnboarded(getApplication(), onboarded)
                _currentUser.value = response.user
                _isOnboarded.value = onboarded
                _isLoggedIn.value = true
            } catch (e: Exception) {
                _error.value = "Login failed. Check your credentials."
            }
            _isLoading.value = false
        }
    }

    fun signUp(name: String, email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = NetworkService.api.signUp(SignUpRequest(name, email, password))
                AuthPreferences.saveToken(getApplication(), response.token)
                val onboarded = response.user.onboarded ?: false
                AuthPreferences.setOnboarded(getApplication(), onboarded)
                _currentUser.value = response.user
                _isOnboarded.value = onboarded
                _isLoggedIn.value = true
            } catch (e: Exception) {
                _error.value = "Sign up failed. Please try again."
            }
            _isLoading.value = false
        }
    }

    fun markOnboarded() {
        viewModelScope.launch {
            AuthPreferences.setOnboarded(getApplication(), true)
            _isOnboarded.value = true
        }
    }

    fun logout() {
        viewModelScope.launch {
            AuthPreferences.clearToken(getApplication())
            _currentUser.value = null
            _isOnboarded.value = true
            _isLoggedIn.value = false
        }
    }
}
