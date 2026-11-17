package com.example.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.DarkModePref
import com.example.ui.theme.ThemePalette
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeDataStore by preferencesDataStore(name = "basari_theme_prefs")

data class ThemePreferences(
    val palette: ThemePalette = ThemePalette.OCEAN,
    val darkModePref: DarkModePref = DarkModePref.SYSTEM
)

class ThemePreferencesRepository(private val context: Context) {
    private val paletteKey = stringPreferencesKey("theme_palette")
    private val darkModeKey = stringPreferencesKey("dark_mode_pref")

    val themePreferences: Flow<ThemePreferences> = context.themeDataStore.data.map { prefs ->
        ThemePreferences(
            palette = ThemePalette.fromKey(prefs[paletteKey]),
            darkModePref = DarkModePref.fromKey(prefs[darkModeKey])
        )
    }

    suspend fun saveTheme(palette: ThemePalette, darkModePref: DarkModePref) {
        context.themeDataStore.edit { prefs ->
            prefs[paletteKey] = palette.key
            prefs[darkModeKey] = darkModePref.key
        }
    }
}
