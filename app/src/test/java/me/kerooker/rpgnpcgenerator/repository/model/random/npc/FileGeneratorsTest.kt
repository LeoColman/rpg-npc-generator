package me.kerooker.rpgnpcgenerator.repository.model.random.npc

import io.kotest.core.spec.style.FreeSpec
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.io.File

class FileGeneratorsTest : FreeSpec({

    "File generators should generate values from their expected file" {
        // Only picking ChildProfessions because it's the smallest file. Less statistical chance to screw up.
        val lines = File("src/main/res/raw/npc_child_professions.txt").readLines()
        val generator = object : FileGenerator(lines) {}

        val generatedSet = List(100_000) { generator.random() }.toSet()

        generatedSet shouldBe lines.toSet()
    }
})

class ProfessionGeneratorTest : FunSpec({

    val commonProfessionGenerator = mockk<CommonProfessionGenerator> {
        every { random() } returns "Common"
    }
    val childProfessionGenerator = mockk<ChildProfessionGenerator> {
        every { random() } returns "Child"
    }

    val target = ProfessionGenerator(
        childProfessionGenerator,
        commonProfessionGenerator
    )

    test("When age is Child, should generate from ChildProfessionGenerator") {
        target.random(Age.Child) shouldBe "Child"
    }

    test("When age is not Child, should generate from CommonProfessionGenerator") {
        Age.values().filterNot { it == Age.Child }.forEach {
            target.random(it) shouldBe "Common"
        }
    }
})
