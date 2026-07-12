package me.kerooker.rpgnpcgenerator

import android.app.Application
import me.kerooker.rpgnpcgenerator.ads.adsModule
import me.kerooker.rpgnpcgenerator.analytics.analyticsModule
import me.kerooker.rpgnpcgenerator.crash.CrashReporting
import me.kerooker.rpgnpcgenerator.data.databaseModule
import me.kerooker.rpgnpcgenerator.repository.image.imageGenModule
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.npcGeneratorsModule
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.randomNpcModule
import me.kerooker.rpgnpcgenerator.ui.theme.themeModule
import me.kerooker.rpgnpcgenerator.viewmodel.my.npc.rosterModule
import me.kerooker.rpgnpcgenerator.viewmodel.viewModelsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class RpgNpcGeneratorApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Before Koin: a crash while wiring the graph is exactly the kind we want reported.
        CrashReporting.init(this)
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
