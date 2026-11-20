package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.ui.theme.CastleThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.lunarDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "lunar_castle_preferences"
)

data class LunarUserPreferences(
    val themeMode: CastleThemeMode = CastleThemeMode.MOONLIT_CASTLE,
    val reducedMotion: Boolean = false,
    val onboardingCompleted: Boolean = false,
    val defaultFocusMinutes: Int = 25,
    val defaultTeaId: String = "earl_grey",
    val studyDeskIntention: String = "Chapter IV Study"
)

class LunarPreferencesRepository(private val context: Context) {

    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val REDUCED_MOTION = booleanPreferencesKey("reduced_motion")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val DEFAULT_FOCUS_MINUTES = intPreferencesKey("default_focus_minutes")
        val DEFAULT_TEA_ID = stringPreferencesKey("default_tea_id")
        val STUDY_DESK_INTENTION = stringPreferencesKey("study_desk_intention")
    }

    val preferencesFlow: Flow<LunarUserPreferences> = context.lunarDataStore.data.map { prefs ->
        val mode = when (prefs[Keys.THEME_MODE]) {
            CastleThemeMode.DAYLIGHT_LIBRARY.name -> CastleThemeMode.DAYLIGHT_LIBRARY
            else -> CastleThemeMode.MOONLIT_CASTLE
        }
        LunarUserPreferences(
            themeMode = mode,
            reducedMotion = prefs[Keys.REDUCED_MOTION] ?: false,
            onboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            defaultFocusMinutes = prefs[Keys.DEFAULT_FOCUS_MINUTES] ?: 25,
            defaultTeaId = prefs[Keys.DEFAULT_TEA_ID] ?: "earl_grey",
            studyDeskIntention = prefs[Keys.STUDY_DESK_INTENTION] ?: "Chapter IV Study"
        )
    }

    suspend fun setThemeMode(mode: CastleThemeMode) {
        context.lunarDataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    suspend fun setReducedMotion(enabled: Boolean) {
        context.lunarDataStore.edit { it[Keys.REDUCED_MOTION] = enabled }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.lunarDataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setDefaultFocusMinutes(minutes: Int) {
        context.lunarDataStore.edit { it[Keys.DEFAULT_FOCUS_MINUTES] = minutes }
    }

    suspend fun setDefaultTeaId(teaId: String) {
        context.lunarDataStore.edit { it[Keys.DEFAULT_TEA_ID] = teaId }
    }

    suspend fun setStudyDeskIntention(intention: String) {
        context.lunarDataStore.edit { it[Keys.STUDY_DESK_INTENTION] = intention.take(120) }
    }

    suspend fun saveMakeItYours(
        themeMode: CastleThemeMode,
        defaultFocusMinutes: Int,
        defaultTeaId: String,
        reducedMotion: Boolean
    ) {
        context.lunarDataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = themeMode.name
            prefs[Keys.DEFAULT_FOCUS_MINUTES] = defaultFocusMinutes
            prefs[Keys.DEFAULT_TEA_ID] = defaultTeaId
            prefs[Keys.REDUCED_MOTION] = reducedMotion
            prefs[Keys.ONBOARDING_COMPLETED] = true
        }
    }
}
