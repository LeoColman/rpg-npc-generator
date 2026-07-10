package me.kerooker.rpgnpcgenerator.repository.image

import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import me.kerooker.rpgnpcgenerator.data.Npc

private const val STYLE = "fantasy character portrait, head and shoulders, detailed face, " +
    "dramatic lighting, painterly, dungeons and dragons, digital painting, artstation, highly detailed"

private const val NEGATIVE_PROMPT = "lowres, bad anatomy, bad hands, extra fingers, extra limbs, " +
    "deformed, disfigured, mutation, text, watermark, signature, blurry, cropped, nsfw"

private fun npc(
    age: String = "Adult",
    gender: String = "Female",
    race: String = "Human",
    profession: String = "Blacksmith",
    alignment: String = "Neutral Good",
    personalityTraits: List<String> = listOf("Brave", "Curious")
) = Npc(
    id = 0,
    fullName = "Aria Nightsong",
    nickname = "The Swift",
    gender = gender,
    sexuality = "Heterosexual",
    race = race,
    age = age,
    profession = profession,
    motivation = "Protect the forest",
    alignment = alignment,
    personalityTraits = personalityTraits,
    languages = listOf("Common"),
    imagePath = null,
    notes = "Met in the tavern"
)

class PortraitPromptTest : FunSpec({

    test("forNpc assembles descriptors from age, gender, race, profession, alignment and traits") {
        val request = PortraitPrompt.forNpc(npc())

        request.prompt shouldBe "Adult, Female, Human, Blacksmith, Neutral Good alignment, Brave, Curious, $STYLE"
    }

    test("forNpc always sets the fixed negative prompt") {
        val request = PortraitPrompt.forNpc(npc())

        request.negativePrompt shouldBe NEGATIVE_PROMPT
    }

    test("forNpc uses the default portrait request dimensions") {
        val request = PortraitPrompt.forNpc(npc())

        request.width shouldBe 512
        request.height shouldBe 640
    }

    test("Drow race matches drow anatomy, not plain elf anatomy") {
        val request = PortraitPrompt.forNpc(npc(race = "Drow"))

        request.prompt shouldContain "obsidian black-grey skin"
        request.prompt shouldNotContain "slender angular ethereal features"
    }

    test("Half-Orc race matches half-orc anatomy, not plain orc anatomy") {
        val request = PortraitPrompt.forNpc(npc(race = "Half-Orc"))

        request.prompt shouldContain "grey-green skin, protruding lower tusks"
        request.prompt shouldNotContain "savage muscular build"
    }

    test("Half-Elf race matches half-elf anatomy, not plain elf anatomy") {
        val request = PortraitPrompt.forNpc(npc(race = "Half-Elf"))

        request.prompt shouldContain "half-human half-elf features"
        request.prompt shouldNotContain "slender angular ethereal features"
    }

    test("race anatomy matching is case-insensitive") {
        val upper = PortraitPrompt.forNpc(npc(race = "DROW"))
        val lower = PortraitPrompt.forNpc(npc(race = "drow"))

        upper.prompt shouldContain "obsidian black-grey skin"
        lower.prompt shouldContain "obsidian black-grey skin"
    }

    test("Portuguese localized race strings map to the correct anatomy") {
        PortraitPrompt.forNpc(npc(race = "Elfo Negro")).prompt shouldContain "obsidian black-grey skin"
        PortraitPrompt.forNpc(npc(race = "Anão")).prompt shouldContain "thick braided beard"
        PortraitPrompt.forNpc(npc(race = "Meio-Orc")).prompt shouldContain "grey-green skin"
    }

    test("race anatomy is matched for every supported race, more specific races taking precedence") {
        val cases = listOf(
            "Dragonborn" to "draconic reptilian humanoid",
            "Tiefling" to "curved ram horns",
            "Drow" to "obsidian black-grey skin",
            "Half-Orc" to "grey-green skin",
            "Half-Elf" to "half-human half-elf features",
            "Dwarf" to "thick braided beard",
            "Halfling" to "small childlike stature",
            "Gnome" to "very small stature",
            "Elf" to "long pointed ears",
            "Orc" to "protruding tusks"
        )

        cases.forAll { (race, expectedAnatomy) ->
            PortraitPrompt.forNpc(npc(race = race)).prompt shouldContain expectedAnatomy
        }
    }

    test("human race passes through unchanged with no anatomy tokens appended") {
        val request = PortraitPrompt.forNpc(npc(race = "Human"))

        request.prompt shouldBe "Adult, Female, Human, Blacksmith, Neutral Good alignment, Brave, Curious, $STYLE"
    }

    test("unknown race passes through unchanged with no anatomy tokens appended") {
        val request = PortraitPrompt.forNpc(npc(race = "Automaton"))

        request.prompt shouldBe "Adult, Female, Automaton, Blacksmith, Neutral Good alignment, Brave, Curious, $STYLE"
    }

    test("blank race is filtered out of descriptors without leaving an empty token") {
        val request = PortraitPrompt.forNpc(npc(race = ""))

        request.prompt shouldBe "Adult, Female, Blacksmith, Neutral Good alignment, Brave, Curious, $STYLE"
    }

    test("blank alignment is omitted entirely, no dangling alignment suffix") {
        val request = PortraitPrompt.forNpc(npc(alignment = ""))

        request.prompt shouldBe "Adult, Female, Human, Blacksmith, Brave, Curious, $STYLE"
    }

    test("only the first two non-blank personality traits are included") {
        val request = PortraitPrompt.forNpc(npc(personalityTraits = listOf("Brave", "Curious", "Loyal", "Greedy")))

        request.prompt shouldContain "Brave, Curious"
        request.prompt shouldNotContain "Loyal"
        request.prompt shouldNotContain "Greedy"
    }

    test("blank personality traits are filtered out before taking the first two") {
        val request = PortraitPrompt.forNpc(npc(personalityTraits = listOf("", "Brave", "", "Curious")))

        request.prompt shouldBe "Adult, Female, Human, Blacksmith, Neutral Good alignment, Brave, Curious, $STYLE"
    }

    test("empty personality traits list produces no trait descriptors") {
        val request = PortraitPrompt.forNpc(npc(personalityTraits = emptyList()))

        request.prompt shouldBe "Adult, Female, Human, Blacksmith, Neutral Good alignment, $STYLE"
    }
})
