package me.kerooker.rpgnpcgenerator.ads

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Base ads wiring, compiled into every flavor. Binds the ad-free entitlement store plus no-op ads
 * seams ([NoOpConsentManager]/[NoOpRewardedAdController]/[NoOpBannerAdRenderer]). The `playstore`
 * flavor overrides the three no-ops with real GMS implementations at runtime (see its adsRealModule),
 * so fdroid/github never touch proprietary ads code.
 */
val adsModule = module {
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.create {
            androidContext().preferencesDataStoreFile("ad_free_prefs")
        }
    }
    single { AdFreeStore(get()) }
    single<ConsentManager> { NoOpConsentManager }
    single<RewardedAdController> { NoOpRewardedAdController }
    single<BannerAdRenderer> { NoOpBannerAdRenderer }
}
