package me.kerooker.rpgnpcgenerator.analytics

import org.koin.dsl.module

/**
 * Base analytics wiring, compiled into every flavor. Binds [NoOpAnalytics] so the FOSS fdroid build
 * sends nothing at all. The signed channels (github/playstore) override this binding at runtime with
 * the real PostHog implementation (see `telemetryModule` in src/telemetry), loaded eagerly so the
 * automatic "Application Opened" lifecycle event (our DAU signal) isn't missed.
 */
val analyticsModule = module {
    single<Analytics>(createdAtStart = true) { NoOpAnalytics }
}
