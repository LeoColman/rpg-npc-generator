package me.kerooker.rpgnpcgenerator

import me.kerooker.rpgnpcgenerator.crash.GlitchTipCrashReporter
import org.koin.core.context.GlobalContext

/**
 * Application for the signed channels that carry analytics + crash reporting (github directly;
 * playstore via [PlaystoreApplication]). Starts GlitchTip before Koin — a crash while wiring the
 * graph is exactly the kind we want reported — then loads [telemetryModule] to rebind the base
 * [me.kerooker.rpgnpcgenerator.analytics.NoOpAnalytics] to the real PostHog implementation.
 *
 * `open` so [PlaystoreApplication] can extend it to additionally rebind the GMS ads.
 */
open class TelemetryApplication : RpgNpcGeneratorApplication() {

    override fun onCreate() {
        // Before Koin: a crash during graph wiring is exactly the kind we want captured.
        GlitchTipCrashReporter.init(this)
        super.onCreate()
        // allowOverride = true so telemetryModule's Analytics binding replaces the base NoOpAnalytics.
        GlobalContext.get().loadModules(
            listOf(telemetryModule),
            allowOverride = true,
            createEagerInstances = true,
        )
    }
}
