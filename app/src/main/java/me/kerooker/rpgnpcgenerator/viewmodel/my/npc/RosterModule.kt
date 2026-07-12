package me.kerooker.rpgnpcgenerator.viewmodel.my.npc

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** DataStore-backed roster preferences (currently the persisted sort order). */
val rosterModule = module {
    single<DataStore<Preferences>>(named(ROSTER_PREFS)) {
        PreferenceDataStoreFactory.create {
            androidContext().preferencesDataStoreFile(ROSTER_PREFS)
        }
    }
    single { RosterPreferences(get(named(ROSTER_PREFS))) }
}

private const val ROSTER_PREFS = "roster_prefs"
