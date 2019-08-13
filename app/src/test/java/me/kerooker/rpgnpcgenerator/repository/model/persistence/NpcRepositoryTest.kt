package me.kerooker.rpgnpcgenerator.repository.model.persistence

import io.kotlintest.*
import io.kotlintest.extensions.TestListener
import io.kotlintest.robolectric.RobolectricExtension
import io.kotlintest.specs.FunSpec
import org.koin.core.KoinComponent
import org.koin.core.context.stopKoin
import org.koin.core.get

class NpcRepositoryTest : FunSpec(), KoinComponent {

    private val repository
        get() = get<NpcRepository>()

    private val npcEntity = NpcEntity(
        "NpcName", "Surname", null, "Gender", "Sexuality", "Race", "Age",
        "Profession", "Motivation", "Alignment", listOf("Personality", "Traits"), emptyList()
    )

    init {
        test("Should persist npc with an id") {
            val id = repository.put(npcEntity)
            id shouldNotBe 0
        }

        test("Should persist and get the same npc") {
            val id = repository.put(npcEntity)
            repository.get(id) shouldBe npcEntity
        }
    }

    override fun isolationMode() = IsolationMode.InstancePerTest

    override fun extensions() = listOf(RobolectricExtension())

    override fun listeners() = listOf<TestListener>(object: TestListener {
        override fun afterTest(testCase: TestCase, result: TestResult) {
            stopKoin()
        }
    })

}