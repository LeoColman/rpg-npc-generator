package me.kerooker.rpgnpcgenerator.data

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe

private fun sampleNpc(
    id: Long = 0,
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
    strength = 12,
    dexterity = 16,
    constitution = 14,
    intelligence = 10,
    wisdom = 13,
    charisma = 11,
    armorClass = 15,
    hitPoints = 22,
    challengeRating = "1/2",
    campaign = "Waterdeep"
)

class RosterBackupTest : FunSpec({

    test("toBackup drops id and imagePath, carries the portrait base64 and every attribute") {
        val npc = sampleNpc(id = 7, imagePath = "/data/portraits/x.jpg")

        val backup = npc.toBackup("BASE64DATA")

        backup.portraitJpegBase64 shouldBe "BASE64DATA"
        backup.fullName shouldBe npc.fullName
        backup.nickname shouldBe npc.nickname
        backup.personalityTraits shouldContainExactly npc.personalityTraits
        backup.languages shouldContainExactly npc.languages
        backup.strength shouldBe npc.strength
        backup.challengeRating shouldBe npc.challengeRating
        backup.campaign shouldBe npc.campaign
    }

    test("toNpc restores a fresh insertable row with id 0 and the given image path") {
        val backup = sampleNpc(id = 42).toBackup(null)

        val restored = backup.toNpc("/new/path.jpg")

        restored.id shouldBe 0
        restored.imagePath shouldBe "/new/path.jpg"
        restored.fullName shouldBe backup.fullName
        restored.languages shouldContainExactly backup.languages
    }

    test("attributes survive a full toBackup then toNpc round-trip") {
        val original = sampleNpc(id = 99, imagePath = "/old/path.jpg")

        val roundTripped = original.toBackup(null).toNpc(null)

        // Everything but the local id and device-local image path is carried through unchanged.
        roundTripped shouldBe original.copy(id = 0, imagePath = null)
    }
})
