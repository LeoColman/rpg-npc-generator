package me.kerooker.rpgnpcgenerator.analytics

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.types.shouldBeSameInstanceAs
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.koinApplication
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Verifies [analyticsModule] wiring in an isolated Koin container (mirrors AdsModuleTest). Unit
 * tests run the debug variant, whose BuildConfig key is blank, so resolving never touches PostHog.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class AnalyticsModuleTest {

    private val context: Application = ApplicationProvider.getApplicationContext()

    @Test
    fun `a blank api key disables analytics via the no-op implementation`() {
        createAnalytics(context, "") shouldBeSameInstanceAs NoOpAnalytics
        createAnalytics(context, "   ") shouldBeSameInstanceAs NoOpAnalytics
    }

    @Test
    fun `analyticsModule resolves Analytics as a shared singleton`() {
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
}
