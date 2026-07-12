package me.kerooker.rpgnpcgenerator.crash

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import io.sentry.Sentry
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * The blank-DSN gate is the whole privacy contract of [CrashReporting]: every debug build ships a
 * blank DSN, and a blank DSN must leave the SDK unstarted so nothing is ever sent from a dev device.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class CrashReportingTest {

    private val context: Application = ApplicationProvider.getApplicationContext()

    @After
    fun tearDown() = Sentry.close()

    @Test
    fun `a blank dsn leaves crash reporting disabled`() {
        CrashReporting.init(context, dsn = "")
        Sentry.isEnabled() shouldBe false

        CrashReporting.init(context, dsn = "   ")
        Sentry.isEnabled() shouldBe false
    }

    @Test
    fun `a real dsn enables crash reporting`() {
        CrashReporting.init(context, dsn = "https://public@glitchtip.example.com/1")
        Sentry.isEnabled() shouldBe true
    }
}
