package me.kerooker.rpgnpcgenerator

import android.app.Application
import me.kerooker.rpgnpcgenerator.ads.adsModule
import me.kerooker.rpgnpcgenerator.analytics.analyticsModule
import me.kerooker.rpgnpcgenerator.data.databaseModule
import me.kerooker.rpgnpcgenerator.repository.image.imageGenModule
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.npcGeneratorsModule
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.randomNpcModule
import me.kerooker.rpgnpcgenerator.ui.theme.themeModule
import me.kerooker.rpgnpcgenerator.viewmodel.my.npc.rosterModule
import me.kerooker.rpgnpcgenerator.viewmodel.viewModelsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * Base application for every flavor: starts Koin with the shared modules (which bind the ad/analytics/
 * crash no-ops). The FOSS `fdroid` build uses this class directly. The signed channels extend it —
 * `TelemetryApplication` (github/playstore) starts crash reporting and rebinds analytics, and
 * `PlaystoreApplication` additionally rebinds the real GMS ads — so no proprietary code is compiled
 * into fdroid. Hence [onCreate] is `open` for those subclasses to wrap.
 */
open class RpgNpcGeneratorApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@RpgNpcGeneratorApplication)
            modules(
                randomNpcModule,
                databaseModule,
                viewModelsModule,
                npcGeneratorsModule,
                imageGenModule,
                adsModule,
                analyticsModule,
                themeModule,
                rosterModule
            )
        }
    }
}
