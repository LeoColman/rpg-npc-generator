package me.kerooker.rpgnpcgenerator.repository.model.random.npc

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldStartWith
import io.kotest.matchers.types.shouldBeInstanceOf
import java.io.File

private fun rawLines(fileName: String): List<String> =
    File("src/main/res/raw/$fileName").readLines()

private fun completeGenerator(): CompleteNpcGenerator {
    val professions = ProfessionGenerator(
        ChildProfessionGenerator(rawLines("npc_child_professions.txt")),
        CommonProfessionGenerator(rawLines("npc_professions.txt"))
    )
    val dataGenerator = NpcDataGenerator(
        NameGenerator(rawLines("npc_names.txt")),
        NicknameGenerator(rawLines("npc_nicknames.txt")),
        professions,
        MotivationGenerator(rawLines("npc_motivations.txt")),
        PersonalityTraitGenerator(rawLines("npc_personality_trait.txt"))
    )
    return CompleteNpcGenerator(dataGenerator, CombatStatsGenerator(), ItemsGenerator(ItemLocale.ENGLISH))
}

class CompleteNpcGeneratorTest : FunSpec() {

    private val target = completeGenerator()
    private val childrenProfessions = rawLines("npc_child_professions.txt").toSet()
    private val normalProfessions = rawLines("npc_professions.txt").toSet()

    private fun generate(amount: Int) = List(amount) { target.generate() }
    private fun generateMany() = generate(100_000)

    init {
        test("Should generate a npc") {
            generate(1).first().shouldBeInstanceOf<GeneratedNpc>()
        }

        test("Should have a .5% chance of not speaking common") {
            generateMany().map { it.languages }.count { CommonLanguage.Common !in it } shouldBeInRange (300..700)
        }

        test("Should have a .5% chance of not speaking racial language") {
            generateMany().map { it.race.racialLanguage to it.languages }.count { (language, spoken) ->
                if (language == null) {
                    false
                } else {
                    language !in spoken
                }
            } shouldBeInRange (300..700)
        }

        test("Should have 25% chance to have an extra non-racial common language") {
            generateMany().map { it.race.racialLanguage to it.languages }.count { (language, spoken) ->
                spoken.any { it != language && it != CommonLanguage.Common && it is CommonLanguage }
            } shouldBeInRange (24_500..25_500)
        }

        test("Should have 5% chance to have an extra non-racial exotic language") {
            generateMany().map { it.race.racialLanguage to it.languages }.count { (language, spoken) ->
                spoken.any { it != language && it is ExoticLanguage }
            } shouldBeInRange (4_700..5_300)
        }

        // A single set-difference assertion rather than shouldContain per generated npc:
        // shouldContain always copies its receiver via toList() and scans it fully to build a
        // possible-failure message, even on success, so calling it per item (up to 100,000 times)
        // against a ~400-line profession list made this test take minutes instead of seconds.
        test("Should generate a child profession if npc is child") {
            val generatedProfessions = generateMany().filter { it.age == Age.Child }.map { it.profession }.toSet()
            generatedProfessions subtract childrenProfessions shouldBe emptySet()
        }

        test("Should generate a normal profession if npc is not a child") {
            val generatedProfessions = generateMany().filterNot { it.age == Age.Child }.map { it.profession }.toSet()
            generatedProfessions subtract normalProfessions shouldBe emptySet()
        }

        test("Should generate between 2 and 5 items, always starting with a coin pouch") {
            generateMany().map { it.items }.forEach {
                it.size shouldBeInRange (ItemsGenerator.MIN_ITEMS..ItemsGenerator.MAX_ITEMS)
                it.first() shouldStartWith ItemsGenerator.COIN_PURSE_PREFIX_EN
            }
        }

        test("Should generate between 2 and 5 personality traits") {
            var twoTraits = 0
            var threeTraits = 0
            var fourTraits = 0
            var fiveTraits = 0

            generateMany().map { it.personalityTraits }.forEach {
                it.size shouldBeInRange (2..5)
                when (it.size) {
                    2 -> twoTraits++
                    3 -> threeTraits++
                    4 -> fourTraits++
                    5 -> fiveTraits++
                }
            }

            twoTraits shouldNotBe 0
            threeTraits shouldNotBe 0
            fourTraits shouldNotBe 0
            fiveTraits shouldNotBe 0
        }
    }
}
