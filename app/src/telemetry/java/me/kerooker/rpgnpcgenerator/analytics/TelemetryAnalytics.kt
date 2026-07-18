package me.kerooker.rpgnpcgenerator.analytics

import android.content.Context
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig

/**
 * Builds the real analytics backend for the signed channels (github/playstore). A blank [apiKey]
 * (every debug build) disables analytics entirely via [NoOpAnalytics].
 */
internal fun createAnalytics(context: Context, apiKey: String): Analytics {
    if (apiKey.isBlank()) return NoOpAnalytics
    val config = PostHogAndroidConfig(apiKey = apiKey, host = POSTHOG_HOST).apply {
        // Lifecycle events (Application Opened/Installed/Updated) drive DAU + version adoption.
        captureApplicationLifecycleEvents = true
        // Single-activity Compose app: activity-based screen tracking would only ever say "Main".
        captureScreenViews = false
        captureDeepLinks = false
    }
    PostHogAndroid.setup(context, config)
    return PostHogAnalytics()
}

private const val POSTHOG_HOST = "https://us.i.posthog.com"
