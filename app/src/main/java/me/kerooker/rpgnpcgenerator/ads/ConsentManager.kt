package me.kerooker.rpgnpcgenerator.ads

import android.app.Activity

/**
 * Gathers ad-consent (Google UMP / GDPR) and initializes the Ads SDK before ads load. Only the
 * `playstore` flavor has a real implementation (backed by the GMS UMP SDK); `fdroid`/`github` bind
 * [NoOpConsentManager], so no proprietary consent/ads code is compiled into those builds.
 */
interface ConsentManager {
    /** Call from the Activity (consent forms need one). [onAdsReady] runs once ads may load. */
    fun ensureConsentAndInit(activity: Activity, onAdsReady: () -> Unit = {})
}

/** Ad-free flavors (fdroid/github): there is no consent to gather and no SDK to initialize. */
object NoOpConsentManager : ConsentManager {
    override fun ensureConsentAndInit(activity: Activity, onAdsReady: () -> Unit) = Unit
}
