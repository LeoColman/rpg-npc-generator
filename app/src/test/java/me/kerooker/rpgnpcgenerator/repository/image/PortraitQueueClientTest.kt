package me.kerooker.rpgnpcgenerator.repository.image

import android.app.Application
import android.util.Base64
import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

private fun client(config: RemoteImageConfig = RemoteImageConfig("https://example.test", "user", "pass")) =
    PortraitQueueClient(config)

/**
 * [PortraitQueueClient.decode] goes through `android.util.Base64`, so it needs Robolectric to run
 * on the JVM. [submit]/[status] are not covered here: they open real sockets via
 * `HttpURLConnection`, which isn't worth faking for a unit test.
 */
@RobolectricTest(sdk = [34], application = Application::class)
class PortraitQueueClientTest : StringSpec({

    "decode decodes a plain base64 string" {
        val expected = "hello world".toByteArray()
        val encoded = Base64.encodeToString(expected, Base64.NO_WRAP)

        val decoded = client().decode(encoded)

        decoded shouldBe expected
    }

    "decode strips a data URI prefix before decoding" {
        val expected = "hello world".toByteArray()
        val encoded = Base64.encodeToString(expected, Base64.NO_WRAP)
        val prefixed = "data:image/png;base64,$encoded"

        val decoded = client().decode(prefixed)

        decoded shouldBe expected
    }

    "decode round-trips arbitrary bytes through encode and decode" {
        val expected = byteArrayOf(0, 1, 2, 3, 127, -128, -1, 42, 100)
        val encoded = Base64.encodeToString(expected, Base64.NO_WRAP)

        val decoded = client().decode(encoded)

        decoded shouldBe expected
    }

    "enabled is true when the injected config is enabled" {
        val config = RemoteImageConfig(baseUrl = "https://example.test", username = "user", password = "pass")

        client(config).enabled shouldBe true
    }

    "enabled is false when the injected config is disabled" {
        val config = RemoteImageConfig(baseUrl = "", username = "user", password = "")

        client(config).enabled shouldBe false
    }
})
