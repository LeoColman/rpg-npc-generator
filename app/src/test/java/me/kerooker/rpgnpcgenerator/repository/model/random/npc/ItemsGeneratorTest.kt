package me.kerooker.rpgnpcgenerator.repository.model.random.npc

import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.ints.shouldBeInRange
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import kotlin.random.Random

private const val SAMPLE_SIZE = 20_000

class ItemsGeneratorTest : FunSpec({

    val english = ItemsGenerator(portuguese = false, random = Random(seed = 20250712))
    val portuguese = ItemsGenerator(portuguese = true, random = Random(seed = 20250712))

    fun ItemsGenerator.rollMany(profession: String) = List(SAMPLE_SIZE) { generate(profession) }

    test("every roll holds between 2 and 5 items (English and Portuguese)") {
        english.rollMany("Blacksmith").forAll { it.size shouldBeInRange (2..5) }
        portuguese.rollMany("Ferreiro").forAll { it.size shouldBeInRange (2..5) }
    }

    test("the coin pouch is always present and always first (English and Portuguese)") {
        // An unknown profession is the worst case: no trade tool, so the pouch could be alone if the
        // 2-item floor didn't hold. It always leads the list.
        english.rollMany("Wanderer").forAll { it.first() shouldStartWith ItemsGenerator.COIN_PURSE_PREFIX_EN }
        portuguese.rollMany("Andarilho").forAll { it.first() shouldStartWith ItemsGenerator.COIN_PURSE_PREFIX_PT }
    }

    test("a known trade always contributes its profession tool, in each locale") {
        english.rollMany("Blacksmith").forAll { it shouldContain "A set of smith's tools" }
        english.rollMany("Alchemist").forAll { it shouldContain "A pouch of alchemical reagents" }
        english.rollMany("Merchant").forAll { it shouldContain "A ledger of debts owed" }
        english.rollMany("Bard").forAll { it shouldContain "A well-loved lute" }

        portuguese.rollMany("Ferreiro").forAll { it shouldContain "Um conjunto de ferramentas de ferreiro" }
        portuguese.rollMany("Alquimista").forAll { it shouldContain "Uma bolsa de reagentes alquímicos" }
        portuguese.rollMany("Mercador").forAll { it shouldContain "Um livro-caixa de dívidas" }
        portuguese.rollMany("Bardo").forAll { it shouldContain "Um alaúde muito estimado" }
    }

    test("an unrecognised profession never yields a trade tool") {
        english.rollMany("Wanderer").forAll { it shouldNotContain "A set of smith's tools" }
    }

    test("items within a single roll are always distinct") {
        english.rollMany("Blacksmith").forAll { it.distinct().size shouldBe it.size }
    }

    test("randomTrinket returns a non-blank flavour item from the table") {
        List(1_000) { english.randomTrinket() }.forAll { it.isNotBlank() shouldBe true }
    }
})
