package me.kerooker.rpgnpcgenerator

import android.app.Application
import me.kerooker.rpgnpcgenerator.legacy.repository.LegacyNpcRepository
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class RpgNpcGeneratorApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin { androidContext(this@RpgNpcGeneratorApplication); modules(koinModule) }
    }
}

val koinModule = module {
    single { LegacyNpcRepository() }
}
