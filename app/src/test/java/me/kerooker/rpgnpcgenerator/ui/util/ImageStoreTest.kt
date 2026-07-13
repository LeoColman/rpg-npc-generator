package me.kerooker.rpgnpcgenerator.ui.util

import android.app.Application
import android.graphics.Bitmap
import androidx.test.core.app.ApplicationProvider
import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith
import java.io.File

private fun tinyBitmap(): Bitmap = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)

/**
 * ImageStore touches `context.filesDir` and real `Bitmap`/`BitmapFactory` behaviour, neither of
 * which a plain JVM unit test can provide, so this runs on Robolectric with a real Context (as
 * NpcFieldTest does for Compose).
 */
@RobolectricTest(sdk = [34], application = Application::class)
class ImageStoreTest : StringSpec({

    lateinit var context: Application
    lateinit var portraitsDir: File

    beforeTest {
        context = ApplicationProvider.getApplicationContext()
        portraitsDir = File(context.filesDir, "portraits")
    }

    "persistBitmap writes a jpeg file inside the portraits directory" {
        val path = ImageStore.persistBitmap(context, tinyBitmap()).shouldNotBeNull()

        path shouldStartWith portraitsDir.absolutePath
        File(path).exists() shouldBe true
    }

    "persistBitmap returns a distinct filename on every call" {
        val first = ImageStore.persistBitmap(context, tinyBitmap()).shouldNotBeNull()
        val second = ImageStore.persistBitmap(context, tinyBitmap()).shouldNotBeNull()

        first shouldNotBe second
        File(first).exists() shouldBe true
        File(second).exists() shouldBe true
    }

    "deletePortrait removes a file located inside the portraits directory" {
        portraitsDir.mkdirs()
        val file = File(portraitsDir, "existing.jpg").apply { writeText("fake-jpeg-bytes") }

        ImageStore.deletePortrait(context, file.absolutePath)

        file.exists() shouldBe false
    }

    "deletePortrait ignores a file directly under filesDir" {
        val file = File(context.filesDir, "not-a-portrait.jpg").apply { writeText("stray") }

        ImageStore.deletePortrait(context, file.absolutePath)

        file.exists() shouldBe true
    }

    "deletePortrait ignores a temp file elsewhere on disk" {
        val tempFile = File.createTempFile("portrait", ".jpg")

        try {
            ImageStore.deletePortrait(context, tempFile.absolutePath)

            tempFile.exists() shouldBe true
        } finally {
            tempFile.delete()
        }
    }
})
