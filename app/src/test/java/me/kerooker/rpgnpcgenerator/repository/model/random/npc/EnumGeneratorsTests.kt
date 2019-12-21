package me.kerooker.rpgnpcgenerator.repository.model.random.npc

import io.kotest.matchers.collections.shouldNotContainDuplicates
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.shouldBe
import io.kotest.specs.FunSpec

class RandomDistributedTest : FunSpec() {

    enum class Randomized(override val distribution: Double):
        RandomDistributed {
        A(19.0),
        B(80.0),
        C(1.0)
    }

    init {
        test("Should have correct weight distribution") {
            val values = Randomized.values().toList()


            var a = 0
            var b = 0
            var c = 0
            repeat(100_000) {
                when(values.distributedRandom()) {
                    Randomized.A -> a++
                    Randomized.B -> b++
                    Randomized.C -> c++
                }
            }

            a shouldBeInRange 18000..20000
            c shouldBeInRange 900..1100
            b shouldBeInRange 79000..81000

        }
    }
}

class EnumDistributionTests : FunSpec() {

    private val randomDistributed: Array<Array<out RandomDistributed>> = arrayOf(
        Age.values(),
        Alignment.values(),
        Gender.values(),
        Race.values(),
        Sexuality.values()
    )

    private val namedResources: Array<Array<out NamedResource>> = arrayOf(
        Age.values(),
        Alignment.values(),
        CommonLanguage.values(),
        ExoticLanguage.values(),
        Gender.values(),
        Race.values(),
        Sexuality.values()
    )

    init {
        test("All enums should have their distribution sum 100%") {
            verifyDistribution(*randomDistributed)
        }

        test("All enum named values should be distinct") {
            verifyDistinctNames(*namedResources)
        }
    }

    private fun verifyDistinctNames(vararg namedResources: Array<out NamedResource>) {
        namedResources.flatten().map { it.nameResource }.shouldNotContainDuplicates()
    }

    private fun verifyDistribution(vararg values: Array<out RandomDistributed>) {
        for (value in values) {
            value.toList().sumByDouble { it.distribution } shouldBe 100.0
        }
    }

}
