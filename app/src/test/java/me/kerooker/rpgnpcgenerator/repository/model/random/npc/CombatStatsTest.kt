package me.kerooker.rpgnpcgenerator.repository.model.random.npc

import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldBe
import kotlin.random.Random

class AbilityModifierTest : FunSpec({

    test("modifier is floor((score - 10) / 2), correct for odd scores below 10 too") {
        abilityModifier(1) shouldBe -5
        abilityModifier(2) shouldBe -4
        abilityModifier(3) shouldBe -4
        abilityModifier(8) shouldBe -1
        abilityModifier(9) shouldBe -1
        abilityModifier(10) shouldBe 0
        abilityModifier(11) shouldBe 0
        abilityModifier(12) shouldBe 1
        abilityModifier(14) shouldBe 2
        abilityModifier(18) shouldBe 4
        abilityModifier(20) shouldBe 5
    }

    test("modifiers render with an explicit sign, including +0") {
        formatAbilityModifier(abilityModifier(1)) shouldBe "-5"
        formatAbilityModifier(abilityModifier(9)) shouldBe "-1"
        formatAbilityModifier(abilityModifier(10)) shouldBe "+0"
        formatAbilityModifier(abilityModifier(11)) shouldBe "+0"
        formatAbilityModifier(abilityModifier(14)) shouldBe "+2"
        formatAbilityModifier(abilityModifier(20)) shouldBe "+5"
    }
})

class CombatStatsGeneratorTest : FunSpec({

    val target = CombatStatsGenerator(Random(seed = 20250710))
    val rolls = List(GENERATED_SAMPLE_SIZE) { target.generate() }

    val abilityRange = CombatStatsGenerator.MIN_ABILITY..CombatStatsGenerator.MAX_ABILITY
    val armorClassRange = CombatStatsGenerator.MIN_ARMOR_CLASS..CombatStatsGenerator.MAX_ARMOR_CLASS
    val hitPointsRange = CombatStatsGenerator.MIN_HIT_POINTS..CombatStatsGenerator.MAX_HIT_POINTS

    test("every ability score lands in the 4d6-drop-lowest range 3..18") {
        rolls.forAll { stats ->
            listOf(
                stats.strength,
                stats.dexterity,
                stats.constitution,
                stats.intelligence,
                stats.wisdom,
                stats.charisma
            ).forAll { it shouldBeInRange abilityRange }
        }
    }

    test("armor class stays within its Dex-derived bounds") {
        rolls.forAll { it.armorClass shouldBeInRange armorClassRange }
    }

    test("hit points are at least 1 and within the Con-derived maximum") {
        rolls.forAll { it.hitPoints shouldBeInRange hitPointsRange }
    }

    test("challenge rating is drawn from the commoner-weighted table") {
        rolls.forAll { CombatStatsGenerator.CHALLENGE_RATINGS shouldContain it.challengeRating }
    }
}) {
    private companion object {
        const val GENERATED_SAMPLE_SIZE = 10_000
    }
}
