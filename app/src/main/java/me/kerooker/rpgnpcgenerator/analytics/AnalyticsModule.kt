package me.kerooker.rpgnpcgenerator.analytics

import android.content.Context
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig
import me.kerooker.rpgnpcgenerator.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Eager ([createdAtStart]) so PostHog is set up when the process starts, not on first injection —
 * otherwise the automatic "Application Opened" lifecycle event (our DAU signal) would be missed.
 */
val analyticsModule = module {
    single<Analytics>(createdAtStart = true) { createAnalytics(androidContext(), BuildConfig.POSTHOG_API_KEY) }
}

/** A blank [apiKey] (every debug build) disables analytics entirely via [NoOpAnalytics]. */
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
