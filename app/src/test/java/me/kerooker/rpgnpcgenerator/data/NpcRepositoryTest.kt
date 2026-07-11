package me.kerooker.rpgnpcgenerator.data

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first

private fun sampleNpc(
    id: Long = 0,
    fullName: String = "Aria Nightsong",
    languages: List<String> = listOf("Common", "Elvish"),
    traits: List<String> = listOf("Brave", "Curious")
) = Npc(
    id = id,
    fullName = fullName,
    nickname = "The Swift",
    gender = "Female",
    sexuality = "Heterosexual",
    race = "High Elf",
    age = "Adult",
    profession = "Ranger",
    motivation = "Protect the forest",
    alignment = "Neutral Good",
    personalityTraits = traits,
    languages = languages,
    imagePath = null,
    notes = "Met in the tavern",
    strength = null,
    dexterity = null,
    constitution = null,
    intelligence = null,
    wisdom = null,
    charisma = null,
    armorClass = null,
    hitPoints = null,
    challengeRating = null
)

class NpcRepositoryTest : FunSpec({

    fun newRepository(): NpcRepository {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        NpcDatabase.Schema.create(driver)
        val database = NpcDatabase(
            driver = driver,
            npcAdapter = Npc.Adapter(
                personalityTraitsAdapter = ListOfStringsAdapter,
                languagesAdapter = ListOfStringsAdapter
            )
        )
        return NpcRepository(database, dispatcher = Dispatchers.Unconfined)
    }

    test("insert assigns an id and stores list columns via the JSON adapter") {
        val repository = newRepository()

        val id = repository.insert(sampleNpc())

        val stored = repository.all().first()
        stored.size shouldBe 1
        stored.first().id shouldBe id
        stored.first().languages shouldContainExactly listOf("Common", "Elvish")
        stored.first().personalityTraits shouldContainExactly listOf("Brave", "Curious")
    }

    test("insertWithId preserves the original id (migration path)") {
        val repository = newRepository()

        repository.insertWithId(sampleNpc(id = 42, fullName = "Old One"))

        val stored = repository.get(42).first()
        stored?.id shouldBe 42
        stored?.fullName shouldBe "Old One"
    }

    test("update mutates an existing row") {
        val repository = newRepository()
        val id = repository.insert(sampleNpc())

        val current = repository.get(id).first()!!
        repository.update(current.copy(notes = "Now an ally", languages = listOf("Common")))

        val updated = repository.get(id).first()!!
        updated.notes shouldBe "Now an ally"
        updated.languages shouldContainExactly listOf("Common")
    }

    test("delete removes the row") {
        val repository = newRepository()
        val id = repository.insert(sampleNpc())

        repository.delete(id)

        repository.all().first() shouldBe emptyList()
    }
})
