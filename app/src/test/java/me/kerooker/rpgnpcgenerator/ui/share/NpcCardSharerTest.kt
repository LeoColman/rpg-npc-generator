package me.kerooker.rpgnpcgenerator.ui.share

import android.app.Application
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.longs.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldEndWith
import java.io.File

private fun tinyBitmap(): Bitmap = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888)

/**
 * NpcCardSharer touches `context.cacheDir` and real `Bitmap.compress`, so (like ImageStoreTest) it
 * runs on Robolectric.
 */
@RobolectricTest(sdk = [34], application = Application::class)
class NpcCardSharerTest : StringSpec({

    lateinit var context: Application

    beforeTest { context = ApplicationProvider.getApplicationContext() }

    "saveCardPng writes a png inside the shared_images cache directory" {
        val file = NpcCardSharer.saveCardPng(context, tinyBitmap())

        val sharedDir = File(context.cacheDir, "shared_images")
        file.parentFile shouldBe sharedDir
        file.name shouldEndWith ".png"
        file.exists() shouldBe true
        file.length() shouldBeGreaterThan 0
    }

    "saveCardPng returns a distinct file on every call" {
        val first = NpcCardSharer.saveCardPng(context, tinyBitmap())
        val second = NpcCardSharer.saveCardPng(context, tinyBitmap())

        first.absolutePath shouldNotBe second.absolutePath
        first.exists() shouldBe true
        second.exists() shouldBe true
    }

    "authority is derived from the running package name" {
        NpcCardSharer.authority(context) shouldBe context.packageName + ".fileprovider"
    }
})
