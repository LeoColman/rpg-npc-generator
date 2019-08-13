package me.kerooker.rpgnpcgenerator.repository.model.npc

import android.content.Context
import io.kotlintest.IsolationMode
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.extensions.TestListener
import io.kotlintest.inspectors.forAll
import io.kotlintest.matchers.collections.shouldContain
import io.kotlintest.matchers.shouldBeInRange
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.robolectric.RobolectricExtension
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.FunSpec
import me.kerooker.rpgnpcgenerator.R
import org.koin.core.KoinComponent
import org.koin.core.context.stopKoin
import org.koin.core.get

class NpcGeneratorTest : FunSpec(), KoinComponent {

    private val target
        get() = get<NpcGenerator>()

    private val childrenProfessions by lazy {
        get<Context>().resources.openRawResource(R.raw.npc_child_professions).bufferedReader().readLines()
    }

    private val normalProfessions by lazy {
        get<Context>().resources.openRawResource(R.raw.npc_professions).bufferedReader().readLines()
    }

    init {
        test("Should generate a npc") {
            generateOne().first().shouldBeInstanceOf<Npc>()
        }

        test("Should generate a null surname with 3% of chance") {
            generateMany().map { it.surname }.count { it == null } shouldBeInRange (2700..3300)
        }

        test("Should have a .5% chance of not speaking common") {
            generateMany().map { it.languages }.count { CommonLanguage.Common !in it } shouldBeInRange (300..700)
        }

        test("Should have a .5% chance of not speaking racial language") {
            generateMany().map { it.race.racialLanguage to it.languages }.count {  (language, spoken) ->
                if(language == null) false
                else language !in spoken
            } shouldBeInRange (300..700)
        }

        test("Should have 25% chance to have an extra non-racial common language") {
            generateMany().map { it.race.racialLanguage to it.languages }.count {  (language, spoken) ->
                spoken.any { it != language && it != CommonLanguage.Common && it is CommonLanguage }
            } shouldBeInRange (24_500..25_500)
        }

        test("Should have 5% chance to have an extra non-racial exotic language") {
            generateMany().map { it.race.racialLanguage to it.languages }.count {  (language, spoken) ->
                spoken.any { it != language && it is ExoticLanguage }
            } shouldBeInRange (4_700..5_300)
        }

        test("Should generate a child profession if npc is child") {
            generateMany().filter { it.age == Age.Child }.forAll {
                childrenProfessions shouldContain it.profession
            }
        }

        test("Should generate a normal profession if npc is not a child") {
            generateMany().filterNot { it.age == Age.Child }.forEach {
                normalProfessions shouldContain it.profession
            }
        }

        test("Should generate between 2 and 5 personality traits") {
            var twoTraits = 0
            var threeTraits = 0
            var fourTraits = 0
            var fiveTraits = 0

            generateMany().map { it.personalityTraits }.forEach {
                it.size shouldBeInRange (2..5)
                when(it.size) {
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

        test("Should generate a nickname in 10% of cases") {
            generateMany().map { it.nickname }.count { it != null } shouldBeInRange (9700..10300)
        }

    }

    private fun generateOne() = generate(1)

    private fun generateMany() = generate(100_000)

    private fun generate(amount: Int) = List(amount) { target.generate() }

    override fun isolationMode() = IsolationMode.InstancePerTest

    override fun extensions() = listOf(RobolectricExtension())

    override fun listeners() = listOf<TestListener>(object: TestListener {
        override fun afterTest(testCase: TestCase, result: TestResult) {
            stopKoin()
        }
    })
}
