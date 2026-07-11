package me.kerooker.rpgnpcgenerator.ui.share

import android.app.Application
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldEndWith
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * NpcCardSharer touches `context.cacheDir` and real `Bitmap.compress`, so (like ImageStoreTest) it
 * runs on Robolectric with GraphicsMode.NATIVE so PNG bytes are actually encoded.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], application = Application::class)
class NpcCardSharerTest {

    private lateinit var context: Application

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    private fun tinyBitmap(): Bitmap = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)

    @Test
    fun `saveCardPng writes a png inside the shared_images cache directory`() {
        val file = NpcCardSharer.saveCardPng(context, tinyBitmap())

        val sharedDir = File(context.cacheDir, "shared_images")
        assertTrue("file should live under cacheDir/shared_images", file.parentFile == sharedDir)
        file.name shouldEndWith ".png"
        assertTrue("png file should exist on disk", file.exists())
        assertTrue("png file should have bytes", file.length() > 0)
    }

    @Test
    fun `saveCardPng returns a distinct file on every call`() {
        val first = NpcCardSharer.saveCardPng(context, tinyBitmap())
        val second = NpcCardSharer.saveCardPng(context, tinyBitmap())

        first.absolutePath shouldNotBe second.absolutePath
        assertTrue(first.exists())
        assertTrue(second.exists())
    }

    @Test
    fun `authority is derived from the running package name`() {
        NpcCardSharer.authority(context) shouldBe context.packageName + ".fileprovider"
    }
}
