package me.kerooker.rpgnpcgenerator.ads

/**
 * Pure entitlement math for the ad-free window, extracted from [AdFreeStore] so it can be unit-tested
 * without Android or DataStore. Times are epoch millis.
 */
object AdEntitlement {

    /** Ads show once the current time has reached the ad-free deadline (default 0 → always on). */
    fun adsEnabled(now: Long, adFreeUntil: Long): Boolean = now >= adFreeUntil

    /** Granting a week extends from whichever is later: now, or an existing (future) deadline. */
    fun grantedUntil(now: Long, currentUntil: Long, weekMillis: Long): Long =
        maxOf(now, currentUntil) + weekMillis
}
