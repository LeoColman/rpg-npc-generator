package me.kerooker.rpgnpcgenerator.ads

import me.kerooker.rpgnpcgenerator.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Rebinds the real GMS ads over the base no-ops from [adsModule]. Loaded by
 * [me.kerooker.rpgnpcgenerator.PlaystoreApplication] on the playstore flavor only, so the AdMob SDK
 * and the ADMOB_* BuildConfig fields never exist in fdroid/github.
 */
val adsRealModule = module {
    single { AdIds(BuildConfig.ADMOB_BANNER_UNIT_ID, BuildConfig.ADMOB_REWARDED_UNIT_ID) }
    single<ConsentManager> { GmsConsentManager(androidContext()) }
    single<RewardedAdController> { GmsRewardedAdController(androidContext(), get()) }
    single<BannerAdRenderer> { GmsBannerAdRenderer }
}
