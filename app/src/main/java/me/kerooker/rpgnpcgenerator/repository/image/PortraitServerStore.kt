package me.kerooker.rpgnpcgenerator.repository.image

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Persists a user override of the portrait server (base URL / username / password) in DataStore,
 * falling back to the baked [defaults] (from BuildConfig) when unset. This is what keeps the portrait
 * feature available in the FOSS (F-Droid) build: the renderer is free software and self-hostable
 * (see server/portrait-renderer), and the user can repoint the app at their own instance — so the
 * network service is neither proprietary nor mandatory.
 *
 * Follows the same DataStore pattern as [me.kerooker.rpgnpcgenerator.ui.theme.ThemePreferenceStore].
 * A stored key wins even when blank (so a user can clear the password to switch portraits off);
 * [reset] removes the override entirely, reverting to the baked defaults.
 */
class PortraitServerStore(
    private val dataStore: DataStore<Preferences>,
    private val defaults: RemoteImageConfig,
) {

    /** The current effective server config: the stored override where set, else the baked defaults. */
    val config: Flow<RemoteImageConfig> = dataStore.data.map { it.toConfig() }

    /** A one-shot snapshot of [config]; used by [PortraitQueueClient] per request. */
    suspend fun current(): RemoteImageConfig = config.first()

    /** Persists a server override. A blank password stored here disables portraits (see RemoteImageConfig). */
    suspend fun save(baseUrl: String, username: String, password: String) {
        dataStore.edit { prefs ->
            prefs[BASE_URL] = baseUrl
            prefs[USERNAME] = username
            prefs[PASSWORD] = password
        }
    }

    /** Clears any override, reverting to the baked BuildConfig defaults. */
    suspend fun reset() {
        dataStore.edit { prefs ->
            prefs.remove(BASE_URL)
            prefs.remove(USERNAME)
            prefs.remove(PASSWORD)
        }
    }

    private fun Preferences.toConfig() = RemoteImageConfig(
        baseUrl = this[BASE_URL] ?: defaults.baseUrl,
        username = this[USERNAME] ?: defaults.username,
        password = this[PASSWORD] ?: defaults.password,
    )

    companion object {
        val BASE_URL = stringPreferencesKey("portrait_server_base_url")
        val USERNAME = stringPreferencesKey("portrait_server_username")
        val PASSWORD = stringPreferencesKey("portrait_server_password")
    }
}
