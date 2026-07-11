package me.kerooker.rpgnpcgenerator.ads

/**
 * AdMob unit IDs for the current build variant. Debug builds get Google's public test units; release
 * builds get the real units (or, absent injected gradle properties, the test units). Fed from
 * `BuildConfig` in [adsModule].
 */
data class AdIds(val bannerUnitId: String, val rewardedUnitId: String)
