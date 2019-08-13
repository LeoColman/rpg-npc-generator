package me.kerooker.rpgnpcgenerator.repository.model.npc

import android.content.Context
import io.kotlintest.IsolationMode
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.extensions.TestListener
import io.kotlintest.robolectric.RobolectricExtension
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
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