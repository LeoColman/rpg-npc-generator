package me.kerooker.rpgnpcgenerator

import android.app.Application
import me.kerooker.rpgnpcgenerator.legacy.repository.LegacyNpcImporter
import me.kerooker.rpgnpcgenerator.legacy.repository.LegacyNpcRepository
import me.kerooker.rpgnpcgenerator.repository.model.persistence.objectBoxModule
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.npcGeneratorsModule
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.randomNpcModule
import me.kerooker.rpgnpcgenerator.viewmodel.viewModelsModule
import org.koin.android.ext.android.inject
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class RpgNpcGeneratorApplication : Application() {

    private val legacyNpcImporter by inject<LegacyNpcImporter>()
        
    override fun onCreate() {
        super.onCreate()

        startKoinModules()
        legacyNpcImporter.importAll()
    }

    private fun startKoinModules() {
        startKoin {
            androidContext(this@RpgNpcGeneratorApplication)
            modules(listOf(mainModule,
                randomNpcModule, objectBoxModule, viewModelsModule, npcGeneratorsModule))
        }
    }
}

val mainModule = module {
    single { LegacyNpcRepository() }
    single { LegacyNpcImporter(get(), get(), get()) }
}
