package me.kerooker.rpgnpcgenerator.ui.theme

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persists the user's [ThemePreference] in DataStore and exposes it as reactive state, following the same
 * pattern as AdFreeStore. Boundary-free by design: the enum<->string mapping is delegated to
 * [ThemePreference] so it can be unit-tested independently of Android.
 */
class ThemePreferenceStore(
    private val dataStore: DataStore<Preferences>,
) {

    /** The current appearance choice, defaulting to [ThemePreference.DEFAULT] until the user changes it. */
    val themePreference: Flow<ThemePreference> =
        dataStore.data.map { ThemePreference.fromStoredValue(it[KEY]) }

    /** Persists [preference]; the whole app re-themes reactively on the next emission. */
    suspend fun setThemePreference(preference: ThemePreference) {
        dataStore.edit { it[KEY] = preference.storedValue }
    }

    companion object {
        val KEY = stringPreferencesKey("theme_preference")
    }
}
