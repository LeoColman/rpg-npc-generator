package me.kerooker.rpgnpcgenerator

import android.app.Application
import me.kerooker.rpgnpcgenerator.data.databaseModule
import me.kerooker.rpgnpcgenerator.repository.image.imageGenModule
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.npcGeneratorsModule
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.randomNpcModule
import me.kerooker.rpgnpcgenerator.viewmodel.viewModelsModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class RpgNpcGeneratorApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@RpgNpcGeneratorApplication)
            modules(randomNpcModule, databaseModule, viewModelsModule, npcGeneratorsModule, imageGenModule)
        }
    }
}
