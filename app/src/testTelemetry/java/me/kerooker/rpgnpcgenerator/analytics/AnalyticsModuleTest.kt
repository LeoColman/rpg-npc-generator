package me.kerooker.rpgnpcgenerator.analytics

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.types.shouldBeSameInstanceAs
import me.kerooker.rpgnpcgenerator.telemetryModule
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.koinApplication

/**
 * Verifies the telemetry analytics wiring for the signed channels. Unit tests run the debug variant,
 * whose PostHog key is blank, so resolving never touches the real PostHog SDK. telemetry test only.
 */
@RobolectricTest(sdk = [34], application = Application::class)
class AnalyticsModuleTest : StringSpec({

    lateinit var context: Application

    beforeTest { context = ApplicationProvider.getApplicationContext() }

    "a blank api key disables analytics via the no-op implementation" {
        createAnalytics(context, "") shouldBeSameInstanceAs NoOpAnalytics
        createAnalytics(context, "   ") shouldBeSameInstanceAs NoOpAnalytics
    }

    "analyticsModule + telemetryModule resolve Analytics as a shared singleton" {
        val koin = koinApplication {
            androidContext(context)
            modules(analyticsModule, telemetryModule)
        }.koin
        try {
            koin.get<Analytics>() shouldBeSameInstanceAs koin.get<Analytics>()
        } finally {
            koin.close()
        }
    }

    // Mirrors the TelemetryApplication runtime path: start the base graph, then loadModules the
    // telemetry override. Guards against a DefinitionOverrideException on the signed-flavor startup.
    "loadModules rebinds Analytics over the base no-op without throwing" {
        val koin = koinApplication {
            androidContext(context)
            modules(analyticsModule)
        }.koin
        try {
            koin.loadModules(listOf(telemetryModule), allowOverride = true, createEagerInstances = true)
            koin.get<Analytics>() shouldBeSameInstanceAs koin.get<Analytics>()
        } finally {
            koin.close()
        }
    }
})
