package me.kerooker.rpgnpcgenerator.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import me.kerooker.rpgnpcgenerator.BuildConfig
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Gathers Google UMP consent (GDPR/EEA) before initializing the Ads SDK — Play policy requires consent
 * to be requested before ads are loaded. On repeat launches consent is cached, so this fast-paths
 * straight to initialization. [MobileAds.initialize] is guarded to run at most once.
 */
class ConsentManager(private val appContext: Context) {

    private val adsInitialized = AtomicBoolean(false)

    /** Call from the Activity (UMP forms need one). [onAdsReady] runs once the SDK is initialized. */
    fun ensureConsentAndInit(activity: Activity, onAdsReady: () -> Unit = {}) {
        val consentInformation = UserMessagingPlatform.getConsentInformation(appContext)
        val params = ConsentRequestParameters.Builder()
            .apply { if (BuildConfig.DEBUG) setConsentDebugSettings(debugSettings(activity)) }
            .build()

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    if (consentInformation.canRequestAds()) initializeAds(onAdsReady)
                }
            },
            {
                // Consent info update failed; non-personalized ads are still permitted.
                if (consentInformation.canRequestAds()) initializeAds(onAdsReady)
            },
        )

        // Fast path when consent was already resolved in a previous session.
        if (consentInformation.canRequestAds()) initializeAds(onAdsReady)
    }

    private fun initializeAds(onAdsReady: () -> Unit) {
        if (adsInitialized.compareAndSet(false, true)) {
            MobileAds.initialize(appContext) { onAdsReady() }
        } else {
            onAdsReady()
        }
    }

    private fun debugSettings(activity: Activity): ConsentDebugSettings =
        ConsentDebugSettings.Builder(activity)
            .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            .build()
}
