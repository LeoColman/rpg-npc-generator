package me.kerooker.rpgnpcgenerator

import me.kerooker.rpgnpcgenerator.ads.adsRealModule
import org.koin.core.context.GlobalContext

/**
 * Google Play application. Extends [TelemetryApplication] (which starts crash reporting and rebinds
 * analytics) and additionally rebinds the real GMS ads over the base no-ops, so the full ad-supported
 * stack is present only on this flavor. Wired via `android:name=".PlaystoreApplication"` in the
 * playstore manifest.
 */
class PlaystoreApplication : TelemetryApplication() {

    override fun onCreate() {
        super.onCreate() // crash init + telemetryModule
        GlobalContext.get().loadModules(listOf(adsRealModule), createEagerInstances = true)
    }
}
