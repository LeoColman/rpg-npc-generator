package me.kerooker.rpgnpcgenerator.repository.model.random.npc

import org.koin.core.KoinComponent
import org.koin.dsl.module
import kotlin.random.Random

val npcGeneratorsModule = module {

}

@Suppress("TooManyFunctions")
class NpcDataGenerator(
    private val nameGenerator: NameGenerator,
    private val nicknameGenerator: NicknameGenerator,
    private val professionGenerator: ProfessionGenerator,
    private val motivationGenerator: MotivationGenerator,
    private val personalityTraitGenerator: PersonalityTraitGenerator
) {
    fun generateFullName() = nameGenerator.random() + " " + nameGenerator.random()

    fun generateNickname() = nicknameGenerator.random()

    fun generateGender() = Gender.random()

    fun generateDistributedGender() = Gender.distributedRandom()

    fun generateSexuality() = Sexuality.random()

    fun generateDistributedSexuality() = Sexuality.distributedRandom()

    fun generateRace() = Race.random()

    fun generateDistributedRace() = Race.distributedRandom()

    fun generateAge() = Age.random()

    fun generateDistributedAge() = Age.distributedRandom()

    fun generateProfession(age: Age) = professionGenerator.random(age)

    fun generateMotivation() = motivationGenerator.random()

    fun generateAlignment() = Alignment.random()

    fun generateDistributedAlignment() = Alignment.distributedRandom()

    fun generatePersonalityTrait() = personalityTraitGenerator.random()

    fun generateRandomLanguage() = Language.random()

    fun generateCommonLanguage(excluding: List<Language>) = CommonLanguage.random(excluding)

    fun generateExoticLanguage(excluding: List<Language>) = ExoticLanguage.random(excluding)
}

class CompleteNpcGenerator(
    private val npcDataGenerator: NpcDataGenerator
) : KoinComponent {

    fun generate(): GeneratedNpc {
        val name = generateFullName()
        val nickname = generateNickname()
        val gender = generateGender()
        val sexuality = generateSexuality()
        val race = generateRace()
        val age = generateAge()
        val profession = generateProfession(age)
        val motivation = generateMotivation()
        val alignment = generateAlignment()
        val personalityTraits = generatePersonalityTraits()
        val languages = generateLanguages(race.racialLanguage)

        return GeneratedNpc(
            name,
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

    private fun generateFullName() = npcDataGenerator.generateFullName()

    private fun generateNickname() = npcDataGenerator.generateNickname()

    private fun generateGender() = npcDataGenerator.generateDistributedGender()

    private fun generateSexuality() = npcDataGenerator.generateDistributedSexuality()

    private fun generateRace() = npcDataGenerator.generateDistributedRace()

    private fun generateAge() = npcDataGenerator.generateDistributedAge()

    private fun generateProfession(age: Age) = npcDataGenerator.generateProfession(age)

    private fun generateMotivation() = npcDataGenerator.generateMotivation()

    private fun generateAlignment() = npcDataGenerator.generateDistributedAlignment()

    private fun generatePersonalityTraits(): MutableList<String> {
        val list = MutableList(2) { npcDataGenerator.generatePersonalityTrait() }
        repeat(MaxAmountOfExtraPersonalityTraits) {
            if(hitsChance(ChanceOfExtraPersonalityTraits))
                list += npcDataGenerator.generatePersonalityTrait()
        }
        return list
    }

    private fun generateLanguages(racialLanguage: Language?): MutableList<Language> {
        return mutableListOf<Language>()
            .tryToAddCommon()
            .tryToAddRacial(racialLanguage)
            .tryToAddExtraCommonLanguage(racialLanguage)
            .tryToAddExtraExoticLanguage(racialLanguage)

    }

    private fun MutableList<Language>.tryToAddCommon() = apply {
        if (hitsChance(ChanceOfSpeakingCommon))
            this += CommonLanguage.Common
    }

    private fun MutableList<Language>.tryToAddRacial(racialLanguage: Language?) = apply {
        if(hitsChance(ChanceOfSpeakingRacial) && racialLanguage != null)
            this += racialLanguage
    }

    private fun MutableList<Language>.tryToAddExtraCommonLanguage(racialLanguage: Language?) = apply {
        if(hitsChance(ChanceOfExtraCommonLanguage))
            this += npcDataGenerator.generateCommonLanguage(this + listOfNotNull(racialLanguage))
    }

    private fun MutableList<Language>.tryToAddExtraExoticLanguage(racialLanguage: Language?) = apply {
        if(hitsChance(ChanceOfExtraExoticLanguage))
            this += npcDataGenerator.generateExoticLanguage(this + listOfNotNull(racialLanguage))
    }

    private fun hitsChance(chance: Double) = Random.nextDouble() <= chance


    companion object {
        private const val ChanceOfSpeakingCommon = 0.995
        private const val ChanceOfSpeakingRacial = 0.995
        private const val ChanceOfExtraCommonLanguage = 0.25
        private const val ChanceOfExtraExoticLanguage = 0.05
        private const val MaxAmountOfExtraPersonalityTraits = 3
        private const val ChanceOfExtraPersonalityTraits = 0.25
    }
}
