package me.kerooker.rpgnpcgenerator.repository.image

import me.kerooker.rpgnpcgenerator.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val imageGenModule = module {
    single {
        RemoteImageConfig(
            baseUrl = BuildConfig.NPC_IMAGE_BASE_URL,
            username = BuildConfig.NPC_IMAGE_USER,
            password = BuildConfig.NPC_IMAGE_PASSWORD
        )
    }
    single { PortraitQueueClient(get()) }
    single { PortraitNotifications(androidContext()) }
}
