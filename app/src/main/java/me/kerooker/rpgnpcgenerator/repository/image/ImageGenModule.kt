package me.kerooker.rpgnpcgenerator.repository.image

import me.kerooker.rpgnpcgenerator.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val imageGenModule = module {
    single { SdModelManager(androidContext()) }
    single {
        RemoteImageConfig(
            baseUrl = BuildConfig.NPC_IMAGE_BASE_URL,
            username = BuildConfig.NPC_IMAGE_USER,
            password = BuildConfig.NPC_IMAGE_PASSWORD
        )
    }
    // Server render first, on-device sd.cpp as the offline fallback.
    single<PortraitGenerator> { RemotePortraitGenerator(get(), AndroidPortraitGenerator(get())) }
    single { PortraitRepository(androidContext(), get()) }
}
