package me.kerooker.rpgnpcgenerator.repository.image

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import me.kerooker.rpgnpcgenerator.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module

val imageGenModule = module {
    // Own qualified DataStore ("portrait_server_prefs") so it stays independent of the ads/theme stores.
    single<DataStore<Preferences>>(named(PORTRAIT_DATASTORE)) {
        PreferenceDataStoreFactory.create {
            androidContext().preferencesDataStoreFile("portrait_server_prefs")
        }
    }
    single {
        PortraitServerStore(
            dataStore = get(named(PORTRAIT_DATASTORE)),
            // Baked BuildConfig values are the defaults; the user can override the server in Settings.
            defaults = RemoteImageConfig(
                baseUrl = BuildConfig.NPC_IMAGE_BASE_URL,
                username = BuildConfig.NPC_IMAGE_USER,
                password = BuildConfig.NPC_IMAGE_PASSWORD,
            ),
        )
    }
    single {
        val store = get<PortraitServerStore>()
        PortraitQueueClient { store.current() }
    }
    single { PortraitNotifications(androidContext()) }
}

private const val PORTRAIT_DATASTORE = "portraitServerPrefs"
