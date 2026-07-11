package me.kerooker.rpgnpcgenerator.ads

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import me.kerooker.rpgnpcgenerator.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val adsModule = module {
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create {
            androidContext().preferencesDataStoreFile("ad_free_prefs")
        }
    }
    single { AdFreeStore(get()) }
    single { AdIds(BuildConfig.ADMOB_BANNER_UNIT_ID, BuildConfig.ADMOB_REWARDED_UNIT_ID) }
    single { ConsentManager(androidContext()) }
    single { RewardedAdController(androidContext(), get()) }
}
