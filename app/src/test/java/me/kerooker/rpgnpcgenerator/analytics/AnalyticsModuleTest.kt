package me.kerooker.rpgnpcgenerator.analytics

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.koinApplication

/**
 * Verifies [analyticsModule] wiring in an isolated Koin container (mirrors AdsModuleTest). Unit
 * tests run the debug variant, whose BuildConfig key is blank, so resolving never touches PostHog.
 */
@RobolectricTest(sdk = [34], application = Application::class)
class AnalyticsModuleTest : StringSpec({

    lateinit var context: Application

    beforeTest { context = ApplicationProvider.getApplicationContext() }

    "a blank api key disables analytics via the no-op implementation" {
        createAnalytics(context, "") shouldBeSameInstanceAs NoOpAnalytics
        createAnalytics(context, "   ") shouldBeSameInstanceAs NoOpAnalytics
    }

    "analyticsModule resolves Analytics as a shared singleton" {
        val koin = koinApplication {
            androidContext(context)
            modules(analyticsModule)
        }.koin
        try {
            koin.get<Analytics>() shouldBeSameInstanceAs koin.get<Analytics>()
        } finally {
            koin.close()
        }
    }
})
