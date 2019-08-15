package me.kerooker.rpgnpcgenerator.repository.model.npc

import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.random.Random

const val CHANCE_OF_SURNAME = 0.97
const val CHANCE_OF_NICKNAME = 0.10
const val CHANCE_OF_SPEAKING_COMMON = 0.995
const val CHANCE_OF_SPEAKING_RACIAL = 0.995
const val CHANCE_OF_EXTRA_COMMON_LANGUAGE = 0.25
const val CHANCE_OF_EXTRA_EXOTIC_LANGUAGE = 0.05
const val MAX_AMOUNT_OF_EXTRA_PERSONALITY_TRAITS = 3
const val CHANCE_OF_EXTRA_PERSONALITY_TRAITS = 0.25

class NpcGenerator : KoinComponent {

    private val nameGenerator by inject<NameGenerator>()
    private val nicknameGenerator by inject<NicknameGenerator>()
    private val professionGenerator by inject<ProfessionGenerator>()
    private val childProfessionGenerator by inject<ChildProfessionGenerator>()
    private val motivationGenerator by inject<MotivationGenerator>()
    private val personalityTraitGenerator by inject<PersonalityTraitGenerator>()

    fun generate(): Npc {
        val name = generateName()
        val surname = generateSurname()
        val nickname = generateNickname()
        val gender = generateGender()
        val sexuality = generateSexuality()
        val race = generateRace()
        val age = generateAge()
        val profession = generateProfession(age)
        val motivation = generateMotivation()
        val alignment = generateAlignment()
        val personalityTraits = generatePersonalityTraits()
        val languages = generateLanguages(race)

        return Npc(
            name,
            surname,
            nickname,
            gender,
            sexuality,
            race,
            age,
            profession,
            motivation,
            alignment,
            personalityTraits,
            languages
        )
    }

    private fun generateName() = nameGenerator.random()

    private fun generateSurname() = if(hitsChance(CHANCE_OF_SURNAME)) nameGenerator.random() else null

    private fun generateNickname() = if(hitsChance(CHANCE_OF_NICKNAME)) nicknameGenerator.random() else null

    private fun generateGender() = Gender.random()

    private fun generateSexuality() = Sexuality.random()

    private fun generateRace() = Race.random()

    private fun generateAge() = Age.random()

    private fun generateProfession(age: Age): String {
        return if(age == Age.Child)
            childProfessionGenerator.random()
        else
            professionGenerator.random()
    }

    private fun generateMotivation() = motivationGenerator.random()

    private fun generateAlignment() = Alignment.random()

    private fun generatePersonalityTraits(): List<String> {
        val list = mutableListOf(personalityTraitGenerator.random(), personalityTraitGenerator.random())
        repeat(MAX_AMOUNT_OF_EXTRA_PERSONALITY_TRAITS) {
            if(hitsChance(CHANCE_OF_EXTRA_PERSONALITY_TRAITS))
                list += personalityTraitGenerator.random()
        }
        return list
    }

    private fun generateLanguages(race: Race): List<Language> {
        return mutableListOf<Language>()
            .tryToAddCommon()
            .tryToAddRacial(race)
            .tryToAddExtraCommonLanguage(race)
            .tryToAddExtraExoticLanguage(race)

    }

    private fun MutableList<Language>.tryToAddCommon() = apply {
        if (hitsChance(CHANCE_OF_SPEAKING_COMMON))
            add(CommonLanguage.Common)
    }

    private fun MutableList<Language>.tryToAddRacial(race: Race) = apply {
        if(hitsChance(CHANCE_OF_SPEAKING_RACIAL)) {
            race.racialLanguage?.let { add(it) }
        }
    }

    private fun MutableList<Language>.tryToAddExtraCommonLanguage(race: Race) = apply {
        if(hitsChance(CHANCE_OF_EXTRA_COMMON_LANGUAGE)) {
            add(
                CommonLanguage.values().filter { it != race.racialLanguage && it != CommonLanguage.Common }.random()
            )
        }
    }

    private fun MutableList<Language>.tryToAddExtraExoticLanguage(race: Race) = apply {
        if(hitsChance(CHANCE_OF_EXTRA_EXOTIC_LANGUAGE)) {
            add(
                ExoticLanguage.values().filter { it != race.racialLanguage }.random()
            )
        }
    }

    private fun hitsChance(chance: Double) = Random.nextDouble() <= chance

}


data class Npc(
    val name: String,
    val surname: String?,
    val nickname: String?,
    val gender: Gender,
    val sexuality: Sexuality,
    val race: Race,
    val age: Age,
    val profession: String,
    val motivation: String,
    val alignment: Alignment,
    val personalityTraits: List<String>,
    val languages: List<Language>
)

