package me.kerooker.rpgnpcgenerator.repository.image

import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.kotest.matchers.string.shouldStartWith
import me.kerooker.rpgnpcgenerator.data.Npc

private const val SFW = "safe for work, fully clothed"

private const val STYLE = "fantasy character portrait, head and shoulders, detailed face, " +
    "dramatic lighting, dungeons and dragons, digital painting, highly detailed"

private const val NEGATIVE_PROMPT = "lowres, bad anatomy, bad hands, extra fingers, extra limbs, " +
    "deformed, disfigured, mutation, text, watermark, signature, blurry, cropped, " +
    "nsfw, nude, nudity, naked, topless, bare chest, exposed breasts, cleavage, underwear, " +
    "lingerie, bikini, swimsuit, sexualized, suggestive, provocative, erotic, revealing clothing"

private fun npc(
    age: String = "Adult",
    gender: String = "Female",
    race: String = "Human",
    profession: String = "Blacksmith",
    alignment: String = "Neutral Good",
    personalityTraits: List<String> = listOf("Brave", "Curious"),
    notes: String = ""
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
    notes = notes,
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

class PortraitPromptTest : FunSpec({

    test("forNpc assembles descriptors from age, gender, race, profession, alignment and traits") {
        val request = PortraitPrompt.forNpc(npc())

        request.prompt shouldBe "$SFW, Adult, Female, Human, Blacksmith, Neutral Good alignment, Brave, Curious, $STYLE"
    }

    test("every prompt begins with the safe-for-work anchor so it survives token truncation") {
        PortraitPrompt.forNpc(npc()).prompt shouldStartWith SFW
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

        request.prompt shouldBe "$SFW, Adult, Female, Human, Blacksmith, Neutral Good alignment, Brave, Curious, $STYLE"
    }

    test("unknown race passes through unchanged with no anatomy tokens appended") {
        val request = PortraitPrompt.forNpc(npc(race = "Automaton"))

        request.prompt shouldBe "$SFW, Adult, Female, Automaton, Blacksmith, Neutral Good alignment, Brave, Curious, $STYLE"
    }

    test("blank race is filtered out of descriptors without leaving an empty token") {
        val request = PortraitPrompt.forNpc(npc(race = ""))

        request.prompt shouldBe "$SFW, Adult, Female, Blacksmith, Neutral Good alignment, Brave, Curious, $STYLE"
    }

    test("blank alignment is omitted entirely, no dangling alignment suffix") {
        val request = PortraitPrompt.forNpc(npc(alignment = ""))

        request.prompt shouldBe "$SFW, Adult, Female, Human, Blacksmith, Brave, Curious, $STYLE"
    }

    test("only the first three non-blank personality traits are included") {
        val request = PortraitPrompt.forNpc(npc(personalityTraits = listOf("Brave", "Curious", "Loyal", "Greedy")))

        request.prompt shouldContain "Brave, Curious, Loyal"
        request.prompt shouldNotContain "Greedy"
    }

    test("blank personality traits are filtered out before taking the first three") {
        val request = PortraitPrompt.forNpc(npc(personalityTraits = listOf("", "Brave", "", "Curious")))

        request.prompt shouldBe "$SFW, Adult, Female, Human, Blacksmith, Neutral Good alignment, Brave, Curious, $STYLE"
    }

    test("empty personality traits list produces no trait descriptors") {
        val request = PortraitPrompt.forNpc(npc(personalityTraits = emptyList()))

        request.prompt shouldBe "$SFW, Adult, Female, Human, Blacksmith, Neutral Good alignment, $STYLE"
    }

    test("child NPCs get the extra wholesome, fully-clothed safety clause") {
        val request = PortraitPrompt.forNpc(npc(age = "Child"))

        request.prompt shouldContain "wholesome, innocent, fully clothed, child-appropriate, modest"
    }

    test("the child safety clause matches the Portuguese age string too") {
        PortraitPrompt.forNpc(npc(age = "Criança")).prompt shouldContain "child-appropriate"
    }

    test("non-child NPCs do not get the child safety clause") {
        PortraitPrompt.forNpc(npc(age = "Adult")).prompt shouldNotContain "child-appropriate"
    }

    test("user notes are woven into the prompt for a saved NPC") {
        val request = PortraitPrompt.forNpc(npc(notes = "long scar across the left cheek, red hooded cloak"))

        request.prompt shouldContain "long scar across the left cheek, red hooded cloak"
    }

    test("blank notes contribute nothing to the prompt") {
        val request = PortraitPrompt.forNpc(npc(notes = ""))

        request.prompt shouldBe "$SFW, Adult, Female, Human, Blacksmith, Neutral Good alignment, Brave, Curious, $STYLE"
    }

    test("long notes are truncated so they cannot blow the token budget") {
        val request = PortraitPrompt.forNpc(npc(notes = "x".repeat(500)))

        request.prompt shouldContain "x".repeat(200)
        request.prompt shouldNotContain "x".repeat(201)
    }

    test("multi-line notes are flattened to a single line") {
        val request = PortraitPrompt.forNpc(npc(notes = "scarred face\nmissing an eye"))

        request.prompt shouldContain "scarred face missing an eye"
    }

    test("items never leak into the portrait prompt, so re-rolling them cannot trigger a re-render") {
        // distinctUntilChanged on the built PortraitRequest collapses item-only changes; the built
        // prompt must be byte-identical whether the NPC carries items or not.
        val withoutItems = PortraitPrompt.forNpc(npc())
        val withItems = PortraitPrompt.forNpc(npc().copy(items = listOf("A set of smith's tools", "A worn dagger")))

        withItems.prompt shouldBe withoutItems.prompt
        withItems.prompt shouldNotContain "smith's tools"
        withItems.prompt shouldNotContain "dagger"
    }
})
