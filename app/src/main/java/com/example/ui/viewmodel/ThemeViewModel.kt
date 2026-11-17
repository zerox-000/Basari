package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.firebase.AcademicRepository
import com.example.data.local.ThemePreferences
import com.example.data.local.ThemePreferencesRepository
import com.example.ui.theme.DarkModePref
import com.example.ui.theme.ThemePalette
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ThemeViewModel(
    private val prefsRepo: ThemePreferencesRepository,
    private val academicRepo: AcademicRepository
) : ViewModel() {

    val themePreferences: StateFlow<ThemePreferences> = prefsRepo.themePreferences
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemePreferences()
        )

    fun selectPalette(palette: ThemePalette) {
        val current = themePreferences.value
        updateTheme(palette, current.darkModePref)
    }

    fun selectDarkMode(darkModePref: DarkModePref) {
        val current = themePreferences.value
        updateTheme(current.palette, darkModePref)
    }

    fun syncFromRemoteIfPresent(paletteKey: String, darkModeKey: String) {
        val p = ThemePalette.fromKey(paletteKey)
        val d = DarkModePref.fromKey(darkModeKey)
        if (p != themePreferences.value.palette || d != themePreferences.value.darkModePref) {
            viewModelScope.launch {
                prefsRepo.saveTheme(p, d)
            }
        }
    }

    private fun updateTheme(palette: ThemePalette, darkModePref: DarkModePref) {
        viewModelScope.launch {
            prefsRepo.saveTheme(palette, darkModePref)
            val uid = Firebase.auth.currentUser?.uid
            if (!uid.isNullOrBlank()) {
                academicRepo.updateThemePreferences(uid, palette.key, darkModePref.key)
            }
        }
    }
}
