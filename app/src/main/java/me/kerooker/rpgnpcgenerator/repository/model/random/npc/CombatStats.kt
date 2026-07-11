package me.kerooker.rpgnpcgenerator.repository.model.random.npc

import kotlin.random.Random

/**
 * An optional D&D 5e combat block that makes an NPC table-ready. Ability scores are plain ints
 * (typically 3-18); [armorClass] and [hitPoints] are ints; [challengeRating] is text so it can hold
 * fractional ratings like "1/8" or "1/4" alongside whole numbers.
 */
data class CombatStats(
    val strength: Int,
    val dexterity: Int,
    val constitution: Int,
    val intelligence: Int,
    val wisdom: Int,
    val charisma: Int,
    val armorClass: Int,
    val hitPoints: Int,
    val challengeRating: String
)

/**
 * The D&D 5e ability modifier for a score: floor((score - 10) / 2). [Math.floorDiv] is used rather
 * than Kotlin integer division because the latter truncates toward zero, which is wrong for odd
 * scores below 10 (e.g. a score of 1 must yield -5, not -4).
 */
fun abilityModifier(score: Int): Int = Math.floorDiv(score - 10, 2)

/** Renders an ability modifier the way a stat block does: "+2", "-1", "+0". */
fun formatAbilityModifier(modifier: Int): String = if (modifier >= 0) "+$modifier" else "$modifier"

/**
 * Rolls a sensible, mostly-commoner combat block. Ability scores use 4d6-drop-lowest (3-18, clustered
 * around 10-13); Armor Class and Hit Points are loosely derived from Dexterity and Constitution; the
 * Challenge Rating is drawn from a low, commoner-weighted table.
 */
class CombatStatsGenerator(
    private val random: Random = Random.Default
) {

    fun generate(): CombatStats {
        val dexterity = rollAbility()
        val constitution = rollAbility()
        return CombatStats(
            strength = rollAbility(),
            dexterity = dexterity,
            constitution = constitution,
            intelligence = rollAbility(),
            wisdom = rollAbility(),
            charisma = rollAbility(),
            armorClass = rollArmorClass(dexterity),
            hitPoints = rollHitPoints(constitution),
            challengeRating = CHALLENGE_RATINGS.random(random)
        )
    }

    /** 4d6, drop the lowest die: yields 3-18 with a realistic bell around 10-13. */
    private fun rollAbility(): Int =
        List(ABILITY_DICE) { random.nextInt(1, DIE_FACES + 1) }
            .sortedDescending()
            .take(ABILITY_DICE_KEPT)
            .sum()

    /** Base 10 + Dex modifier + a small amount of "armor" (0-6). */
    private fun rollArmorClass(dexterity: Int): Int =
        BASE_ARMOR_CLASS + abilityModifier(dexterity) + random.nextInt(0, MAX_ARMOR_BONUS + 1)

    /** A handful of hit dice (avg 5 each) padded by the Con modifier per die; never below 1. */
    private fun rollHitPoints(constitution: Int): Int {
        val hitDice = random.nextInt(MIN_HIT_DICE, MAX_HIT_DICE + 1)
        return (hitDice * (HIT_DIE_AVERAGE + abilityModifier(constitution))).coerceAtLeast(1)
    }

    companion object {
        private const val ABILITY_DICE = 4
        private const val ABILITY_DICE_KEPT = 3
        private const val DIE_FACES = 6

        private const val BASE_ARMOR_CLASS = 10
        private const val MAX_ARMOR_BONUS = 6

        private const val MIN_HIT_DICE = 1
        private const val MAX_HIT_DICE = 3
        private const val HIT_DIE_AVERAGE = 5

        // Ability modifiers reachable from the 3-18 roll range: floor((3-10)/2)=-4, floor((18-10)/2)=+4.
        private const val MIN_ABILITY_MODIFIER = -4
        private const val MAX_ABILITY_MODIFIER = 4

        const val MIN_ABILITY = 3
        const val MAX_ABILITY = 18

        const val MIN_ARMOR_CLASS = BASE_ARMOR_CLASS + MIN_ABILITY_MODIFIER
        const val MAX_ARMOR_CLASS = BASE_ARMOR_CLASS + MAX_ABILITY_MODIFIER + MAX_ARMOR_BONUS
        const val MIN_HIT_POINTS = 1
        const val MAX_HIT_POINTS = MAX_HIT_DICE * (HIT_DIE_AVERAGE + MAX_ABILITY_MODIFIER)

        // A commoner-heavy Challenge Rating table: low ratings dominate, nothing above 2.
        val CHALLENGE_RATINGS = listOf("0", "0", "0", "1/8", "1/8", "1/4", "1/4", "1/2", "1", "2")
    }
}
