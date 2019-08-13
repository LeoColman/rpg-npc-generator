package me.kerooker.rpgnpcgenerator

import android.app.Application
import me.kerooker.rpgnpcgenerator.legacy.repository.LegacyNpcRepository
import me.kerooker.rpgnpcgenerator.repository.model.npc.NpcGenerator
import me.kerooker.rpgnpcgenerator.repository.model.npc.fileGeneratorModule
import me.kerooker.rpgnpcgenerator.repository.model.persistence.objectBoxModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class RpgNpcGeneratorApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoinModules()
    }

    private fun startKoinModules() {
        startKoin {
            androidContext(this@RpgNpcGeneratorApplication)
            modules(listOf(koinModule, fileGeneratorModule, objectBoxModule))
        }
    }
}

val koinModule = module {
    single { LegacyNpcRepository() }
    single { NpcGenerator() }
}
