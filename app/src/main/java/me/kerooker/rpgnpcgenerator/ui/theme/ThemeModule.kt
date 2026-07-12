package me.kerooker.rpgnpcgenerator.ui.theme

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * DI for the appearance/theme preference. Uses its own qualified [DataStore] (a separate "theme_prefs"
 * file) so it stays fully independent of the ads DataStore — a plain unqualified single would clash with
 * the one declared in adsModule, since both are DataStore<Preferences>.
 */
val themeModule = module {
    single<DataStore<Preferences>>(named(THEME_DATASTORE)) {
        PreferenceDataStoreFactory.create {
            androidContext().preferencesDataStoreFile("theme_prefs")
        }
    }
    single { ThemePreferenceStore(get(named(THEME_DATASTORE))) }
}

private const val THEME_DATASTORE = "themePrefs"
