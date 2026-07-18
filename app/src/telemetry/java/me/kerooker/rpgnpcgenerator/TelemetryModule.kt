package me.kerooker.rpgnpcgenerator

import me.kerooker.rpgnpcgenerator.analytics.Analytics
import me.kerooker.rpgnpcgenerator.analytics.createAnalytics
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * Rebinds the real PostHog analytics over the base [me.kerooker.rpgnpcgenerator.analytics.NoOpAnalytics].
 * Loaded (eagerly) by [TelemetryApplication] on the signed channels only. Eager so the automatic
 * "Application Opened" lifecycle event (our DAU signal) fires at startup, not on first injection.
 */
val telemetryModule = module {
    single<Analytics>(createdAtStart = true) {
        createAnalytics(androidContext(), BuildConfig.POSTHOG_API_KEY)
    }
}
