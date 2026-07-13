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
import org.koin.core.KoinApplication
import org.koin.dsl.koinApplication

/**
 * koin-test isn't on this module's test classpath (no `org.koin.test.verify.verify()` available),
 * so imageGenModule is verified by hand: build an isolated KoinApplication from it - not
 * `startKoin`/`stopKoin`, so this can't collide with Koin's GlobalContext if other tests in the
 * suite start their own - and resolve every declared type, exactly like verify() would assert.
 * Robolectric supplies the real Context that `androidContext(...)` and `PortraitNotifications`
 * both need.
 */
@RobolectricTest(sdk = [34], application = Application::class)
class ImageGenModuleTest : StringSpec({

    lateinit var koinApp: KoinApplication

    beforeTest {
        val context: Application = ApplicationProvider.getApplicationContext()
        koinApp = koinApplication {
            androidContext(context)
            modules(imageGenModule)
        }
    }

    afterTest { koinApp.close() }

    "every definition declared by the module resolves" {
        val koin = koinApp.koin

        koin.get<RemoteImageConfig>().shouldBeInstanceOf<RemoteImageConfig>()
        koin.get<PortraitQueueClient>().shouldBeInstanceOf<PortraitQueueClient>()
        koin.get<PortraitNotifications>().shouldBeInstanceOf<PortraitNotifications>()
    }

    "RemoteImageConfig is wired from BuildConfig fields" {
        val config = koinApp.koin.get<RemoteImageConfig>()

        config.baseUrl shouldBe BuildConfig.NPC_IMAGE_BASE_URL
        config.username shouldBe BuildConfig.NPC_IMAGE_USER
        config.password shouldBe BuildConfig.NPC_IMAGE_PASSWORD
    }

    "RemoteImageConfig and PortraitQueueClient are shared singletons" {
        val koin = koinApp.koin

        koin.get<RemoteImageConfig>() shouldBeSameInstanceAs koin.get<RemoteImageConfig>()
        koin.get<PortraitQueueClient>() shouldBeSameInstanceAs koin.get<PortraitQueueClient>()
    }
})
