package me.kerooker.rpgnpcgenerator.repository.image

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import me.kerooker.rpgnpcgenerator.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.koinApplication

/**
 * koin-test isn't on this module's test classpath (no `org.koin.test.verify.verify()` available),
 * so imageGenModule is verified by hand: build an isolated KoinApplication from it - not
 * `startKoin`/`stopKoin`, so this can't collide with Koin's GlobalContext if other tests in the
 * suite start their own - and resolve every declared type, exactly like verify() would assert.
 *
 * Built exactly once (a single test method) because the module creates a real [PortraitServerStore]
 * DataStore for its file, and DataStore forbids multiple active instances for the same file.
 */
@RobolectricTest(sdk = [34], application = Application::class)
class ImageGenModuleTest : StringSpec({

    "resolves its definitions, defaults the server to BuildConfig, and shares singletons" {
        val context: Application = ApplicationProvider.getApplicationContext()
        val koinApp = koinApplication {
            androidContext(context)
            modules(imageGenModule)
        }
        try {
            val koin = koinApp.koin

            koin.get<PortraitServerStore>().shouldBeInstanceOf<PortraitServerStore>()
            koin.get<PortraitQueueClient>().shouldBeInstanceOf<PortraitQueueClient>()
            koin.get<PortraitNotifications>().shouldBeInstanceOf<PortraitNotifications>()

            // The store defaults to the baked BuildConfig server until the user overrides it in Settings.
            val config = koin.get<PortraitServerStore>().current()
            config.baseUrl shouldBe BuildConfig.NPC_IMAGE_BASE_URL
            config.username shouldBe BuildConfig.NPC_IMAGE_USER
            config.password shouldBe BuildConfig.NPC_IMAGE_PASSWORD

            koin.get<PortraitServerStore>() shouldBeSameInstanceAs koin.get<PortraitServerStore>()
            koin.get<PortraitQueueClient>() shouldBeSameInstanceAs koin.get<PortraitQueueClient>()
        } finally {
            koinApp.close()
        }
    }
})
