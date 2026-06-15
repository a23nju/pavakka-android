package ai.laennec.pavakka.core.services

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "pavakka_prefs")

object AuthPreferences {
    private val TOKEN_KEY = stringPreferencesKey("auth_token")

    var token: String? = null
        private set

    suspend fun init(context: Context) {
        token = context.dataStore.data.map { it[TOKEN_KEY] }.first()
    }

    suspend fun saveToken(context: Context, newToken: String) {
        token = newToken
        context.dataStore.edit { it[TOKEN_KEY] = newToken }
    }

    suspend fun clearToken(context: Context) {
        token = null
        context.dataStore.edit { it.remove(TOKEN_KEY) }
    }
}
