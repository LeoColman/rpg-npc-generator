package me.kerooker.rpgnpcgenerator.ui.share

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.string.shouldStartWith
import me.kerooker.rpgnpcgenerator.data.Npc

private val sampleNpc = Npc(
    id = 0,
    fullName = "Aria Nightsong",
    nickname = "The Swift",
    gender = "Female",
    sexuality = "Heterosexual",
    race = "Human",
    age = "Adult",
    profession = "Blacksmith",
    motivation = "Protect the forest",
    alignment = "Neutral Good",
    personalityTraits = listOf("Brave", "Curious"),
    languages = listOf("Common"),
    imagePath = null,
    notes = "",
    strength = null,
    dexterity = null,
    constitution = null,
    intelligence = null,
    wisdom = null,
    charisma = null,
    armorClass = null,
    hitPoints = null,
    challengeRating = null,
    campaign = null,
    items = emptyList()
)

class NpcShareTextTest : FunSpec({

    val footer = "Created with RPG NPC Generator"

    test("leads with the name and ends with the footer watermark") {
        val text = npcShareText(sampleNpc, footer)

        text shouldStartWith "Aria Nightsong"
        text shouldEndWith footer
    }

    test("includes nickname, meta line and key attributes") {
        val text = npcShareText(sampleNpc, footer)

        text shouldContain "“The Swift”"
        text shouldContain "Human · Adult · Female"
        text shouldContain "Blacksmith"
        text shouldContain "Neutral Good"
        text shouldContain "Protect the forest"
        text shouldContain "Brave · Curious"
    }

    test("includes the items line when the npc carries items, joined by the separator") {
        val text = npcShareText(
            sampleNpc.copy(items = listOf("Coin pouch (7 copper)", "A worn dagger")),
            footer
        )

        text shouldContain "Coin pouch (7 copper) · A worn dagger"
    }

    test("omits the items line entirely when the npc has none") {
        val text = npcShareText(sampleNpc.copy(items = emptyList()), footer)

        text shouldNotContain "copper"
    }

    test("skips blank attributes instead of emitting empty lines") {
        val text = npcShareText(
            sampleNpc.copy(nickname = "", profession = "  ", personalityTraits = listOf("", "  ")),
            footer
        )

        text shouldNotContain "“"
        text shouldNotContain "Blacksmith"
        // No run of three newlines: blanks are dropped rather than left as empty lines.
        text shouldNotContain "\n\n\n"
    }

    test("still produces a usable body when only the name is present") {
        val bare = sampleNpc.copy(
            nickname = "",
            gender = "",
            race = "",
            age = "",
            profession = "",
            motivation = "",
            alignment = "",
            personalityTraits = emptyList()
        )

        val text = npcShareText(bare, footer)

        text shouldStartWith "Aria Nightsong"
        text shouldEndWith footer
    }
})
