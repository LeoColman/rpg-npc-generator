package me.kerooker.rpgnpcgenerator.ui.util

import android.app.Application
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * ImageStore touches `context.filesDir` and real `Bitmap`/`BitmapFactory` behaviour, neither of
 * which a plain JVM unit test can provide, so this runs on Robolectric with a real Context (as
 * NpcFieldTest does for Compose). GraphicsMode.NATIVE is needed so `Bitmap.compress` actually
 * encodes bytes instead of being an unshadowed no-op.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], application = Application::class)
class ImageStoreTest {

    private lateinit var context: Application
    private lateinit var portraitsDir: File

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        portraitsDir = File(context.filesDir, "portraits")
    }

    private fun tinyBitmap(): Bitmap = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)

    @Test
    fun `persistBitmap writes a jpeg file inside the portraits directory`() {
        val path = ImageStore.persistBitmap(context, tinyBitmap()).shouldNotBeNull()

        path shouldStartWith portraitsDir.absolutePath
        assertTrue("persisted file should exist on disk", File(path).exists())
    }

    @Test
    fun `persistBitmap returns a distinct filename on every call`() {
        val first = ImageStore.persistBitmap(context, tinyBitmap()).shouldNotBeNull()
        val second = ImageStore.persistBitmap(context, tinyBitmap()).shouldNotBeNull()

        first shouldNotBe second
        assertTrue(File(first).exists())
        assertTrue(File(second).exists())
    }

    @Test
    fun `deletePortrait removes a file located inside the portraits directory`() {
        portraitsDir.mkdirs()
        val file = File(portraitsDir, "existing.jpg").apply { writeText("fake-jpeg-bytes") }

        ImageStore.deletePortrait(context, file.absolutePath)

        file.exists() shouldBe false
    }

    @Test
    fun `deletePortrait ignores a file directly under filesDir`() {
        val file = File(context.filesDir, "not-a-portrait.jpg").apply { writeText("stray") }

        ImageStore.deletePortrait(context, file.absolutePath)

        file.exists() shouldBe true
    }

    @Test
    fun `deletePortrait ignores a temp file elsewhere on disk`() {
        val tempFile = File.createTempFile("portrait", ".jpg")

        try {
            ImageStore.deletePortrait(context, tempFile.absolutePath)

            tempFile.exists() shouldBe true
        } finally {
            tempFile.delete()
        }
    }
}
