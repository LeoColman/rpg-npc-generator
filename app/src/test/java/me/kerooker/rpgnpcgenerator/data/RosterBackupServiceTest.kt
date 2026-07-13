package me.kerooker.rpgnpcgenerator.data

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldEndWith
import java.io.File

private fun npc(
    id: Long = 1,
    fullName: String = "Aria Nightsong",
    imagePath: String? = null
) = Npc(
    id = id,
    fullName = fullName,
    nickname = "The Swift",
    gender = "Female",
    sexuality = "Heterosexual",
    race = "High Elf",
    age = "Adult",
    profession = "Ranger",
    motivation = "Protect the forest",
    alignment = "Neutral Good",
    personalityTraits = listOf("Brave", "Curious"),
    languages = listOf("Common", "Elvish"),
    imagePath = imagePath,
    notes = "Met in the tavern",
    strength = null,
    dexterity = null,
    constitution = null,
    intelligence = null,
    wisdom = null,
    charisma = null,
    armorClass = null,
    hitPoints = null,
    challengeRating = null,
    campaign = "Waterdeep",
    items = emptyList()
)

private fun writePortraitFile(context: Application, bytes: ByteArray): String {
    val directory = File(context.filesDir, "portraits").apply { mkdirs() }
    val file = File(directory, "source-portrait.jpg")
    file.writeBytes(bytes)
    return file.absolutePath
}

/**
 * Exercises the real file + base64 paths of [RosterBackupService], so (like the other storage-touching
 * suites) it runs on Robolectric for a genuine `context.filesDir`.
 */
@RobolectricTest(sdk = [34], application = Application::class)
class RosterBackupServiceTest : StringSpec({

    lateinit var context: Application

    beforeTest { context = ApplicationProvider.getApplicationContext() }

    "export then import round-trips an npc with its portrait bytes into a new file" {
        val portraitBytes = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
        val sourcePath = writePortraitFile(context, portraitBytes)
        val original = npc(id = 5, fullName = "Aria", imagePath = sourcePath)

        val json = RosterBackupService.exportJson(listOf(original))
        val imported = RosterBackupService.importNpcs(context, json)

        imported.size shouldBe 1
        val restored = imported.first()
        restored.id shouldBe 0
        restored.fullName shouldBe "Aria"
        restored.campaign shouldBe "Waterdeep"
        restored.imagePath shouldNotBe null
        restored.imagePath shouldNotBe sourcePath // a fresh file, not the original path
        restored.imagePath!! shouldEndWith ".jpg"
        File(restored.imagePath!!).readBytes().toList() shouldBe portraitBytes.toList()
    }

    "an npc without a portrait round-trips with a null image path" {
        val json = RosterBackupService.exportJson(listOf(npc(imagePath = null)))

        RosterBackupService.importNpcs(context, json).first().imagePath shouldBe null
    }

    "export encodes every npc under a version and import restores them all" {
        val json = RosterBackupService.exportJson(
            listOf(npc(fullName = "A", imagePath = null), npc(fullName = "B", imagePath = null))
        )

        json.contains("\"version\"") shouldBe true
        val imported = RosterBackupService.importNpcs(context, json)
        imported.map { it.fullName } shouldBe listOf("A", "B")
    }

    "importing a file that is not a backup throws" {
        shouldThrow<Exception> {
            RosterBackupService.importNpcs(context, "this is not a backup file")
        }
    }

    "a portrait whose file is missing exports as no portrait rather than failing" {
        val original = npc(imagePath = "/nonexistent/portrait.jpg")

        val json = RosterBackupService.exportJson(listOf(original))

        RosterBackupService.importNpcs(context, json).first().imagePath shouldBe null
    }
})
