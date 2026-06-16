package ai.laennec.pavakka.core.services

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "pavakka_prefs")

object AuthPreferences {
    private val TOKEN_KEY = stringPreferencesKey("auth_token")
    private val ONBOARDED_KEY = booleanPreferencesKey("onboarded")

    var token: String? = null
        private set

    // Default true so existing users are never blocked; new signups set it false.
    var onboarded: Boolean = true
        private set

    suspend fun init(context: Context) {
        token = context.dataStore.data.map { it[TOKEN_KEY] }.first()
        onboarded = context.dataStore.data.map { it[ONBOARDED_KEY] }.first() ?: true
    }

    suspend fun saveToken(context: Context, newToken: String) {
        token = newToken
        context.dataStore.edit { it[TOKEN_KEY] = newToken }
    }

    suspend fun setOnboarded(context: Context, value: Boolean) {
        onboarded = value
        context.dataStore.edit { it[ONBOARDED_KEY] = value }
    }

    suspend fun clearToken(context: Context) {
        token = null
        onboarded = true
        context.dataStore.edit { it.remove(TOKEN_KEY); it.remove(ONBOARDED_KEY) }
    }
}
