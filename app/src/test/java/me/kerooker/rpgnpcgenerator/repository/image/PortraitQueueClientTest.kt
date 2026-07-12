package me.kerooker.rpgnpcgenerator.repository.image

import android.app.Application
import android.util.Base64
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * [PortraitQueueClient.decode] goes through `android.util.Base64`, so it needs Robolectric to run
 * on the JVM. [submit]/[status] are not covered here: they open real sockets via
 * `HttpURLConnection`, which isn't worth faking for a unit test.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class PortraitQueueClientTest {

    private fun client(config: RemoteImageConfig = RemoteImageConfig("https://example.test", "user", "pass")) =
        PortraitQueueClient(config)

    @Test
    fun `decode decodes a plain base64 string`() {
        val expected = "hello world".toByteArray()
        val encoded = Base64.encodeToString(expected, Base64.NO_WRAP)

        val decoded = client().decode(encoded)

        decoded shouldBe expected
    }

    @Test
    fun `decode strips a data URI prefix before decoding`() {
        val expected = "hello world".toByteArray()
        val encoded = Base64.encodeToString(expected, Base64.NO_WRAP)
        val prefixed = "data:image/png;base64,$encoded"

        val decoded = client().decode(prefixed)

        decoded shouldBe expected
    }

    @Test
    fun `decode round-trips arbitrary bytes through encode and decode`() {
        val expected = byteArrayOf(0, 1, 2, 3, 127, -128, -1, 42, 100)
        val encoded = Base64.encodeToString(expected, Base64.NO_WRAP)

        val decoded = client().decode(encoded)

        decoded shouldBe expected
    }

    @Test
    fun `enabled is true when the injected config is enabled`() {
        val config = RemoteImageConfig(baseUrl = "https://example.test", username = "user", password = "pass")

        client(config).enabled shouldBe true
    }

    @Test
    fun `enabled is false when the injected config is disabled`() {
        val config = RemoteImageConfig(baseUrl = "", username = "user", password = "")

        client(config).enabled shouldBe false
    }
}
