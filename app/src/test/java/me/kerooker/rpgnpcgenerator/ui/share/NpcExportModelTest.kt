package me.kerooker.rpgnpcgenerator.ui.share

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import me.kerooker.rpgnpcgenerator.data.Npc

private val statlessNpc = Npc(
    id = 0,
    fullName = "Aria Nightsong",
    nickname = "",
    gender = "Female",
    sexuality = "Heterosexual",
    race = "Human",
    age = "Adult",
    profession = "Blacksmith",
    motivation = "Protect the forest",
    alignment = "Neutral Good",
    personalityTraits = listOf("Brave"),
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
    campaign = null
)

class NpcExportModelTest : FunSpec({

    context("hasCombatStats") {
        test("is false when every combat column is null or blank") {
            statlessNpc.hasCombatStats() shouldBe false
            statlessNpc.copy(challengeRating = "   ").hasCombatStats() shouldBe false
        }

        test("is true when any ability score is present") {
            statlessNpc.copy(strength = 14).hasCombatStats() shouldBe true
        }

        test("is true when a derived stat is present") {
            statlessNpc.copy(armorClass = 12).hasCombatStats() shouldBe true
            statlessNpc.copy(hitPoints = 8).hasCombatStats() shouldBe true
        }

        test("is true when only the challenge rating is filled in") {
            statlessNpc.copy(challengeRating = "1/4").hasCombatStats() shouldBe true
        }
    }

    context("formatAbilityScore") {
        test("appends the D&D 5e modifier to the score") {
            formatAbilityScore(14) shouldBe "14 (+2)"
            formatAbilityScore(10) shouldBe "10 (+0)"
            formatAbilityScore(8) shouldBe "8 (-1)"
        }

        test("floors odd low scores toward the lower modifier") {
            // floor((1 - 10) / 2) = -5, not -4 (truncation would be wrong here).
            formatAbilityScore(1) shouldBe "1 (-5)"
        }

        test("returns null for a missing score so callers can skip the cell") {
            formatAbilityScore(null) shouldBe null
        }
    }
})
