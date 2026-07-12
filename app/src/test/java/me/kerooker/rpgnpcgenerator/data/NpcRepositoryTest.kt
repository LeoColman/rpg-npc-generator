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
    traits: List<String> = listOf("Brave", "Curious"),
    campaign: String? = null,
    items: List<String> = emptyList()
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
    challengeRating = null,
    campaign = campaign,
    items = items
)

class NpcRepositoryTest : FunSpec({

    fun newRepository(): NpcRepository {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        NpcDatabase.Schema.create(driver)
        val database = NpcDatabase(
            driver = driver,
            npcAdapter = Npc.Adapter(
                personalityTraitsAdapter = ListOfStringsAdapter,
                languagesAdapter = ListOfStringsAdapter,
                itemsAdapter = ListOfStringsAdapter
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

    test("items round-trip through insert and update via the JSON adapter") {
        val repository = newRepository()
        val id = repository.insert(sampleNpc(items = listOf("Coin pouch (5 copper)", "A worn dagger")))

        repository.get(id).first()!!.items shouldContainExactly listOf("Coin pouch (5 copper)", "A worn dagger")

        repository.update(repository.get(id).first()!!.copy(items = listOf("A holy symbol on a cord")))
        repository.get(id).first()!!.items shouldContainExactly listOf("A holy symbol on a cord")
    }

    test("campaign round-trips through insert and update") {
        val repository = newRepository()
        val id = repository.insert(sampleNpc(campaign = "Waterdeep"))

        repository.get(id).first()!!.campaign shouldBe "Waterdeep"

        repository.update(repository.get(id).first()!!.copy(campaign = null))
        repository.get(id).first()!!.campaign shouldBe null
    }

    test("distinctCampaigns returns non-blank campaigns once each, ordered case-insensitively") {
        val repository = newRepository()
        repository.insert(sampleNpc(fullName = "A", campaign = "Waterdeep"))
        repository.insert(sampleNpc(fullName = "B", campaign = "Waterdeep"))
        repository.insert(sampleNpc(fullName = "C", campaign = "avernus"))
        repository.insert(sampleNpc(fullName = "D", campaign = "Baldur's Gate"))
        repository.insert(sampleNpc(fullName = "E", campaign = null))
        repository.insert(sampleNpc(fullName = "F", campaign = "   "))

        repository.distinctCampaigns().first() shouldContainExactly listOf("avernus", "Baldur's Gate", "Waterdeep")
    }

    test("setTags stores tags, sanitizes them and exposes them per-npc") {
        val repository = newRepository()
        val id = repository.insert(sampleNpc())

        repository.setTags(id, listOf(" Villain ", "villain", "", "Boss"))

        // Duplicates (case-insensitive) and blanks are dropped, first spelling wins; queries sort by tag.
        repository.tagsFor(id).first() shouldContainExactly listOf("Boss", "Villain")
        repository.allTags().first()[id] shouldContainExactly listOf("Boss", "Villain")
    }

    test("setTags replaces the previous tags") {
        val repository = newRepository()
        val id = repository.insert(sampleNpc())

        repository.setTags(id, listOf("Old"))
        repository.setTags(id, listOf("New"))

        repository.tagsFor(id).first() shouldContainExactly listOf("New")
    }

    test("distinctTags returns each tag once, ordered case-insensitively") {
        val repository = newRepository()
        val a = repository.insert(sampleNpc(fullName = "A"))
        val b = repository.insert(sampleNpc(fullName = "B"))
        repository.setTags(a, listOf("Villain", "Ally"))
        repository.setTags(b, listOf("Villain", "Boss"))

        repository.distinctTags().first() shouldContainExactly listOf("Ally", "Boss", "Villain")
    }

    test("deleting an npc also removes its tags") {
        val repository = newRepository()
        val id = repository.insert(sampleNpc())
        repository.setTags(id, listOf("Villain"))

        repository.delete(id)

        repository.tagsFor(id).first() shouldBe emptyList()
        repository.allTags().first() shouldBe emptyMap()
    }

    test("migrating from schema v2 to v3 creates a usable npc_tag table") {
        // A device on the pre-tags schema (v2) upgrades: the 2.sqm migration must add npc_tag.
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        NpcDatabase.Schema.migrate(driver, oldVersion = 2, newVersion = 3)
        val database = NpcDatabase(
            driver = driver,
            npcAdapter = Npc.Adapter(
                personalityTraitsAdapter = ListOfStringsAdapter,
                languagesAdapter = ListOfStringsAdapter,
                itemsAdapter = ListOfStringsAdapter
            )
        )
        val repository = NpcRepository(database, dispatcher = Dispatchers.Unconfined)

        repository.setTags(1L, listOf("Villain"))

        repository.allTags().first() shouldBe mapOf(1L to listOf("Villain"))
    }

    test("migrating from schema v3 to v4 adds items, defaulting pre-existing rows to an empty list") {
        // Stand up the v3 npc table (no items column) with one legacy row, then apply 3.sqm.
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        driver.execute(null, V3_NPC_TABLE, 0)
        driver.execute(null, INSERT_LEGACY_NPC, 0)

        NpcDatabase.Schema.migrate(driver, oldVersion = 3, newVersion = 4)

        val database = NpcDatabase(
            driver = driver,
            npcAdapter = Npc.Adapter(
                personalityTraitsAdapter = ListOfStringsAdapter,
                languagesAdapter = ListOfStringsAdapter,
                itemsAdapter = ListOfStringsAdapter
            )
        )
        val repository = NpcRepository(database, dispatcher = Dispatchers.Unconfined)

        // The pre-existing row survives the upgrade and simply starts with no items.
        val migrated = repository.all().first().single()
        migrated.fullName shouldBe "Old One"
        migrated.items shouldBe emptyList()

        // The migrated column is fully usable afterwards.
        repository.update(migrated.copy(items = listOf("A worn dagger")))
        repository.get(migrated.id).first()!!.items shouldContainExactly listOf("A worn dagger")
    }
})

// The npc table exactly as it stood at schema v3 (before the items column). Used by the v3->v4
// migration test to reproduce a real pre-upgrade database.
private val V3_NPC_TABLE = """
    CREATE TABLE npc (
        id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
        fullName TEXT NOT NULL,
        nickname TEXT NOT NULL,
        gender TEXT NOT NULL,
        sexuality TEXT NOT NULL,
        race TEXT NOT NULL,
        age TEXT NOT NULL,
        profession TEXT NOT NULL,
        motivation TEXT NOT NULL,
        alignment TEXT NOT NULL,
        personalityTraits TEXT NOT NULL,
        languages TEXT NOT NULL,
        imagePath TEXT,
        notes TEXT NOT NULL DEFAULT '',
        strength INTEGER,
        dexterity INTEGER,
        constitution INTEGER,
        intelligence INTEGER,
        wisdom INTEGER,
        charisma INTEGER,
        armorClass INTEGER,
        hitPoints INTEGER,
        challengeRating TEXT,
        campaign TEXT
    );
""".trimIndent()

private val INSERT_LEGACY_NPC = """
    INSERT INTO npc(fullName, nickname, gender, sexuality, race, age, profession, motivation,
        alignment, personalityTraits, languages, notes)
    VALUES ('Old One', '', '', '', '', '', '', '', '', '["Brave"]', '["Common"]', '');
""".trimIndent()
