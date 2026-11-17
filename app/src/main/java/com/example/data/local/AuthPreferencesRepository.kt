package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.authDataStore by preferencesDataStore(name = "basari_auth_prefs")

data class AuthPreferences(
    val rememberLogin: Boolean = true,
    val savedEmail: String = "",
    val savedDisplayName: String = "",
    val lastSignedInAtMillis: Long = 0L
)

class AuthPreferencesRepository(private val context: Context) {
    private val rememberLoginKey = booleanPreferencesKey("remember_login")
    private val savedEmailKey = stringPreferencesKey("saved_email")
    private val savedDisplayNameKey = stringPreferencesKey("saved_display_name")
    private val lastSignedInAtKey = longPreferencesKey("last_signed_in_at")

    val authPreferences: Flow<AuthPreferences> = context.authDataStore.data.map { prefs ->
        AuthPreferences(
            rememberLogin = prefs[rememberLoginKey] ?: true,
            savedEmail = prefs[savedEmailKey].orEmpty(),
            savedDisplayName = prefs[savedDisplayNameKey].orEmpty(),
            lastSignedInAtMillis = prefs[lastSignedInAtKey] ?: 0L
        )
    }

    suspend fun saveSignedInAccount(
        email: String?,
        displayName: String?,
        rememberLogin: Boolean
    ) {
        context.authDataStore.edit { prefs ->
            prefs[rememberLoginKey] = rememberLogin
            if (rememberLogin) {
                prefs[savedEmailKey] = email?.trim().orEmpty()
                prefs[savedDisplayNameKey] = displayName?.trim().orEmpty()
                prefs[lastSignedInAtKey] = System.currentTimeMillis()
            } else {
                prefs.remove(savedEmailKey)
                prefs.remove(savedDisplayNameKey)
                prefs.remove(lastSignedInAtKey)
            }
        }
    }

    suspend fun setRememberLogin(enabled: Boolean) {
        context.authDataStore.edit { prefs ->
            prefs[rememberLoginKey] = enabled
            if (!enabled) {
                prefs.remove(savedEmailKey)
                prefs.remove(savedDisplayNameKey)
                prefs.remove(lastSignedInAtKey)
            }
        }
    }

    suspend fun clearSavedAccount() {
        context.authDataStore.edit { prefs ->
            prefs.remove(savedEmailKey)
            prefs.remove(savedDisplayNameKey)
            prefs.remove(lastSignedInAtKey)
        }
    }
}
