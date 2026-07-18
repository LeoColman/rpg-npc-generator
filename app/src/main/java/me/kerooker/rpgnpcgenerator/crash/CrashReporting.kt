package me.kerooker.rpgnpcgenerator.crash

/**
 * Shared crash-reporting entry point, compiled into every flavor. The real Sentry/GlitchTip
 * integration lives in src/telemetry (GlitchTipCrashReporter) and is started from the flavor
 * Application, so the FOSS fdroid build carries no crash-reporting SDK at all. Only the SDK-free test
 * hook remains here so Settings compiles for every flavor.
 */
object CrashReporting {

    /**
     * Throws an uncaught exception so a developer can confirm, after any change to the reporting
     * setup, that crashes really do reach the collector. Debug builds only — the Settings entry that
     * calls it is compiled behind the same [me.kerooker.rpgnpcgenerator.BuildConfig.DEBUG] check, so
     * it can't reach users.
     */
    fun forceTestCrash(): Nothing = error("Test crash from Settings — verifying crash reporting")
}
