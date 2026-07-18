package me.kerooker.rpgnpcgenerator.ads

/**
 * AdMob unit IDs for the current build variant. Debug builds get Google's public test units; release
 * builds get the real units (or, absent injected gradle properties, the test units). Fed from
 * `BuildConfig` in the playstore adsRealModule. playstore-flavor only — the FOSS build never reads
 * the ADMOB_* fields, which don't exist there.
 */
data class AdIds(val bannerUnitId: String, val rewardedUnitId: String)
