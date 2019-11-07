package me.kerooker.rpgnpcgenerator.repository.model.random.npc

import android.content.Context
import io.kotlintest.IsolationMode
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.extensions.TestListener
import io.kotlintest.robolectric.RobolectricExtension
import io.kotlintest.robolectric.SkipRobolectric
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import io.kotlintest.specs.FunSpec
import io.mockk.every
import io.mockk.mockk
import me.kerooker.rpgnpcgenerator.R
import org.koin.core.KoinComponent
import org.koin.core.context.stopKoin
import org.koin.core.get


class FileGeneratorsTest : FreeSpec(), KoinComponent {

    init {
        "File generators should generate values from their expected file" {
            // Only picking ChildProfessions because it's the smallest file. Less statistical chance to screw up
            val generator = object : FileGenerator(R.raw.npc_child_professions, get()) { }

            val generatedSet = List(100_000) { generator.random() }.toSet()

            generatedSet shouldBe getAllLinesFromFile()
        }
    }

    private fun getAllLinesFromFile() =
        get<Context>().resources.openRawResource(R.raw.npc_child_professions).bufferedReader().readLines().toSet()

    override fun isolationMode() = IsolationMode.InstancePerTest

    override fun extensions() = listOf(RobolectricExtension())

    override fun listeners() = listOf<TestListener>(object: TestListener {
        override fun afterTest(testCase: TestCase, result: TestResult) {
            stopKoin()
        }
    })
}

@SkipRobolectric
class ProfessionGeneratorTest : FunSpec() {

    private val commonProfessionGenerator = mockk<CommonProfessionGenerator> {
        every { random() } returns "Common"
    }
    private val childProfessionGenerator = mockk<ChildProfessionGenerator> {
        every { random() } returns "Child"
    }

    private val target = ProfessionGenerator(
        childProfessionGenerator,
        commonProfessionGenerator
    )

    init {
        test("When age is Child, should generate from ChildProfessionGenerator") {
            target.random(Age.Child) shouldBe "Child"
        }

        test("When age is not Child, should generate from CommonProfessionGenerator") {
            Age.values().filterNot { it == Age.Child }.forEach {
                target.random(it) shouldBe "Common"
            }
        }
    }

    override fun isolationMode() = IsolationMode.InstancePerTest
}