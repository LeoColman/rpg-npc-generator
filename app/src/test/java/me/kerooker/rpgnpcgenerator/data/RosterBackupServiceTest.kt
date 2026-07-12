package me.kerooker.rpgnpcgenerator.data

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldEndWith
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Exercises the real file + base64 paths of [RosterBackupService], so (like the other storage-touching
 * suites) it runs on Robolectric for a genuine `context.filesDir`.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class RosterBackupServiceTest {

    private lateinit var context: Application

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

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

    private fun writePortraitFile(bytes: ByteArray): String {
        val directory = File(context.filesDir, "portraits").apply { mkdirs() }
        val file = File(directory, "source-portrait.jpg")
        file.writeBytes(bytes)
        return file.absolutePath
    }

    @Test
    fun `export then import round-trips an npc with its portrait bytes into a new file`() {
        val portraitBytes = byteArrayOf(1, 2, 3, 4, 5, 6, 7, 8)
        val sourcePath = writePortraitFile(portraitBytes)
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

    @Test
    fun `an npc without a portrait round-trips with a null image path`() {
        val json = RosterBackupService.exportJson(listOf(npc(imagePath = null)))

        RosterBackupService.importNpcs(context, json).first().imagePath shouldBe null
    }

    @Test
    fun `export encodes every npc under a version and import restores them all`() {
        val json = RosterBackupService.exportJson(
            listOf(npc(fullName = "A", imagePath = null), npc(fullName = "B", imagePath = null))
        )

        json.contains("\"version\"") shouldBe true
        val imported = RosterBackupService.importNpcs(context, json)
        imported.map { it.fullName } shouldBe listOf("A", "B")
    }

    @Test
    fun `importing a file that is not a backup throws`() {
        assertThrows(Exception::class.java) {
            RosterBackupService.importNpcs(context, "this is not a backup file")
        }
    }

    @Test
    fun `a portrait whose file is missing exports as no portrait rather than failing`() {
        val original = npc(imagePath = "/nonexistent/portrait.jpg")

        val json = RosterBackupService.exportJson(listOf(original))

        RosterBackupService.importNpcs(context, json).first().imagePath shouldBe null
    }
}
