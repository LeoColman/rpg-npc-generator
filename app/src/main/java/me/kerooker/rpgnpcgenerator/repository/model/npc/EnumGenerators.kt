@file:Suppress("MagicNumber")
package me.kerooker.rpgnpcgenerator.repository.model.npc

import androidx.annotation.StringRes
import me.kerooker.rpgnpcgenerator.R
import kotlin.random.Random

interface RandomDistributed {
    val distribution: Double
}

interface NamedResource {
    val nameResource: Int
}


enum class Age(
    @StringRes override val nameResource: Int,
    override val distribution: Double
) : RandomDistributed, NamedResource {

    Child(R.string.age_child, 5.0),
    Teenager(R.string.age_teenager, 10.0),
    YoungAdult(R.string.age_young_adult, 35.0),
    Adult(R.string.age_adult, 35.0),
    Old(R.string.age_old, 10.0),
    VeryOld(R.string.age_very_old, 5.0);

    companion object {
        fun random() = values().toList().random()
    }
}

enum class Alignment(
    @StringRes override val nameResource: Int,
    override val distribution: Double
) : RandomDistributed, NamedResource {

    LawfulGood(R.string.alignment_lawful_good, 2.0),
    LawfulNeutral(R.string.alignment_lawful_neutral,10.0),
    LawfulEvil(R.string.alignment_lawful_evil, 2.0),
    NeutralGood(R.string.alignment_neutral_good, 10.0),
    Neutral(R.string.alignment_neutral, 52.0),
    NeutralEvil(R.string.alignment_neutral_evil, 10.0),
    ChaoticGood(R.string.alignment_chaotic_good, 2.0),
    ChaoticNeutral(R.string.alignment_chaotic_neutral, 10.0),
    ChaoticEvil(R.string.alignment_chaotic_evil, 2.0);

    companion object {
        fun random() = values().toList().random()
    }

}

enum class Gender(
    @StringRes override val nameResource: Int,
    override val distribution: Double
) : RandomDistributed, NamedResource {

    Male(R.string.gender_male, 50.0),
    Female(R.string.gender_female, 50.0);

    companion object {
        fun random() = values().toList().random()
    }
}

interface Language : NamedResource

enum class CommonLanguage(
    @StringRes override val nameResource: Int
) : Language {
    Common(R.string.language_common),
    Dwarvish(R.string.language_dwarvish),
    Elvish(R.string.language_elvish),
    Giant(R.string.language_giant),
    Gnomish(R.string.language_gnomish),
    Goblin(R.string.language_goblin),
    Halfling(R.string.language_halfling),
    Orc(R.string.language_orc)
}

enum class ExoticLanguage(
    @StringRes override val nameResource: Int
) : Language {
    Celestial(R.string.language_celestial),
    Abyssal(R.string.language_abyssal),
    Infernal(R.string.language_infernal),
    Druidic(R.string.language_druidic),
    Draconic(R.string.language_draconic)
}

enum class Race(
    @StringRes override val nameResource: Int,
    override val distribution: Double,
    val racialLanguage: Language?
) : RandomDistributed, NamedResource {

    HillDwarf(R.string.race_hill_dwarf, 8.75, CommonLanguage.Dwarvish),
    MountainDwarf(R.string.race_mountain_dwarf, 8.75, CommonLanguage.Dwarvish),

    HighElf(R.string.race_high_elf, 5.84, CommonLanguage.Elvish),
    WoodElf(R.string.race_wood_elf, 5.83, CommonLanguage.Elvish),
    Drow(R.string.race_drow, 5.83, CommonLanguage.Elvish),

    ForestHalfling(R.string.race_forest_halfling, 8.75, CommonLanguage.Halfling),
    RockHalfling(R.string.race_rock_halfling, 8.75, CommonLanguage.Halfling),

    Human(R.string.race_human, 17.5, null),

    Dragonborn(R.string.race_dragonborn, 6.0, ExoticLanguage.Draconic),

    ForestGnome(R.string.race_forest_gnome, 3.0, CommonLanguage.Gnomish),
    RockGnome(R.string.race_rock_gnome, 3.0, CommonLanguage.Gnomish),

    HalfElf(R.string.race_half_elf, 6.0, CommonLanguage.Elvish),

    HalfOrc(R.string.race_half_orc, 6.0, CommonLanguage.Orc),

    Tiefling(R.string.race_tiefling, 6.0, ExoticLanguage.Infernal);

    companion object {
        fun random() = values().toList().random()
    }
}

enum class Sexuality(
    @StringRes override val nameResource: Int,
    override val distribution: Double
) : RandomDistributed, NamedResource {

    Homosexual(R.string.sexuality_homosexual, 2.0),
    Bisexual(R.string.sexuality_bisexual, 2.0),
    Asexual(R.string.sexuality_asexual, 1.0),
    Heterosexual(R.string.sexuality_heterosexual, 95.0);

    companion object {
        fun random() = values().toList().random()
    }

}


// Code inspired from https://github.com/thomasnield/kotlin-statistics/blob/master/src/main/kotlin/org/nield/kotlinstatistics/Random.kt#L111
fun <T: RandomDistributed> List<T>.random(): T {
    val probabilities = associateWith { it.distribution }

    val sum = probabilities.values.sum()

    val rangedDistribution = probabilities.run {

        var binStart = 0.0

        asSequence().sortedBy { it.value }
            .map { it.key to OpenDoubleRange(binStart, it.value + binStart) }
            .onEach { binStart = it.second.endExclusive }
            .toMap()
    }

    return Random.nextDouble(0.0, sum).let {
        rangedDistribution.asIterable().first { rng -> it in rng.value }.key
    }
}

private data class OpenDoubleRange(val start: Double, val endExclusive: Double) {
    operator fun contains(it: Double): Boolean {
        return it >=start && it < endExclusive
    }

}