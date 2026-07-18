package me.kerooker.rpgnpcgenerator.crash

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.sentry.Sentry

/**
 * The blank-DSN gate is the whole privacy contract of [GlitchTipCrashReporter]: every debug build
 * ships a blank DSN, and a blank DSN must leave the SDK unstarted so nothing is ever sent from a dev
 * device. telemetry test only — the FOSS fdroid build has no Sentry SDK.
 */
@RobolectricTest(sdk = [34], application = Application::class)
class GlitchTipCrashReporterTest : StringSpec({

    lateinit var context: Application

    beforeTest { context = ApplicationProvider.getApplicationContext() }
    afterTest { Sentry.close() }

    "a blank dsn leaves crash reporting disabled" {
        GlitchTipCrashReporter.init(context, dsn = "")
        Sentry.isEnabled() shouldBe false

        GlitchTipCrashReporter.init(context, dsn = "   ")
        Sentry.isEnabled() shouldBe false
    }

    "a real dsn enables crash reporting" {
        GlitchTipCrashReporter.init(context, dsn = "https://public@glitchtip.example.com/1")
        Sentry.isEnabled() shouldBe true
    }
})
