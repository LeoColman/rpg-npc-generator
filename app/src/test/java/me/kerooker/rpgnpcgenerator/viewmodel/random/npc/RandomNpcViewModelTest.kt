package me.kerooker.rpgnpcgenerator.viewmodel.random.npc

import android.content.Context
import io.kotest.assertions.withClue
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import me.kerooker.rpgnpcgenerator.analytics.NoOpAnalytics
import me.kerooker.rpgnpcgenerator.data.Npc
import me.kerooker.rpgnpcgenerator.data.NpcRepository
import me.kerooker.rpgnpcgenerator.repository.image.PortraitPrompt
import me.kerooker.rpgnpcgenerator.repository.image.PortraitQueueClient
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.Age
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.Alignment
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.CombatStats
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.CommonLanguage
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.CompleteNpcGenerator
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.Gender
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.GeneratedNpc
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.GeneratedNpcData
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.NpcDataGenerator
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.Race
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.Sexuality
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.TemporaryRandomNpcRepository

private val ORIGINAL_COMBAT = CombatStats(
    strength = 10,
    dexterity = 10,
    constitution = 10,
    intelligence = 10,
    wisdom = 10,
    charisma = 10,
    armorClass = 12,
    hitPoints = 8,
    challengeRating = "0"
)

private val REROLLED_COMBAT = CombatStats(
    strength = 18,
    dexterity = 16,
    constitution = 15,
    intelligence = 8,
    wisdom = 12,
    charisma = 14,
    armorClass = 17,
    hitPoints = 27,
    challengeRating = "2"
)

private fun sampleNpcData(combat: CombatStats?, items: List<String> = listOf("Coin pouch (5 copper)")) =
    GeneratedNpcData(
        name = "Aria Nightsong",
        nickname = "The Swift",
        gender = "Female",
        sexuality = "Heterosexual",
        race = "Human",
        age = "Adult",
        profession = "Blacksmith",
        motivation = "Protect the forest",
        alignment = "Neutral Good",
        personalityTraits = listOf("Brave", "Curious"),
        languages = listOf("Common"),
        combat = combat,
        items = items
    )

/** The NPC on screen before a "randomize all"; every field is a distinct "Current…" marker. */
private val CURRENT_DATA = GeneratedNpcData(
    name = "CurrentName",
    nickname = "CurrentNick",
    gender = "CurrentGender",
    sexuality = "CurrentSexuality",
    race = "CurrentRace",
    age = "CurrentAge",
    profession = "CurrentProfession",
    motivation = "CurrentMotivation",
    alignment = "CurrentAlignment",
    personalityTraits = listOf("CurrentTrait"),
    languages = listOf("CurrentLang"),
    combat = ORIGINAL_COMBAT,
    items = listOf("CurrentItem")
)

/** The fresh roll that [CompleteNpcGenerator.generate] is stubbed to return; all "Fresh…" values. */
private val FRESH_NPC = GeneratedNpc(
    name = "FreshName",
    nickname = "FreshNick",
    gender = Gender.Male,
    sexuality = Sexuality.Heterosexual,
    race = Race.Human,
    age = Age.Adult,
    profession = "FreshProfession",
    motivation = "FreshMotivation",
    alignment = Alignment.Neutral,
    personalityTraits = listOf("FreshTrait"),
    languages = listOf(CommonLanguage.Common),
    combat = REROLLED_COMBAT,
    items = listOf("FreshItem")
)

/** Reads the on-screen value of a given lockable field, for asserting it survived (or changed). */
private fun accessorFor(field: LockableField): (GeneratedNpcData) -> Any? = when (field) {
    LockableField.NAME -> { d -> d.name }
    LockableField.NICKNAME -> { d -> d.nickname }
    LockableField.RACE -> { d -> d.race }
    LockableField.AGE -> { d -> d.age }
    LockableField.GENDER -> { d -> d.gender }
    LockableField.SEXUALITY -> { d -> d.sexuality }
    LockableField.PROFESSION -> { d -> d.profession }
    LockableField.ALIGNMENT -> { d -> d.alignment }
    LockableField.MOTIVATION -> { d -> d.motivation }
    LockableField.LANGUAGES -> { d -> d.languages }
    LockableField.PERSONALITY -> { d -> d.personalityTraits }
    LockableField.COMBAT -> { d -> d.combat }
    LockableField.ITEMS -> { d -> d.items }
}

/**
 * Mirrors RandomNpcViewModel's private GeneratedNpcData.toNpc() mapping so the test can build the
 * exact same [Npc] the portrait pipeline would build from [PortraitPrompt.forNpc], without widening
 * the viewmodel's visibility just for a test.
 */
private fun GeneratedNpcData.toPortraitNpc() = Npc(
    id = 0,
    fullName = name,
    nickname = nickname,
    gender = gender,
    sexuality = sexuality,
    race = race,
    age = age,
    profession = profession,
    motivation = motivation,
    alignment = alignment,
    personalityTraits = personalityTraits,
    languages = languages,
    imagePath = null,
    notes = "",
    strength = combat?.strength?.toLong(),
    dexterity = combat?.dexterity?.toLong(),
    constitution = combat?.constitution?.toLong(),
    intelligence = combat?.intelligence?.toLong(),
    wisdom = combat?.wisdom?.toLong(),
    charisma = combat?.charisma?.toLong(),
    armorClass = combat?.armorClass?.toLong(),
    hitPoints = combat?.hitPoints?.toLong(),
    challengeRating = combat?.challengeRating,
    campaign = null,
    items = items
)

class RandomNpcViewModelTest : FunSpec({

    beforeSpec {
        // viewModelScope requires Dispatchers.Main to be initialized on the JVM.
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    fun buildViewModel(
        repository: TemporaryRandomNpcRepository,
        completeNpcGenerator: CompleteNpcGenerator,
        npcDataGenerator: NpcDataGenerator = mockk(),
        context: Context = mockk(relaxed = true)
    ): RandomNpcViewModel {
        val npcRepository = mockk<NpcRepository>(relaxed = true)
        // enabled defaults to false on a relaxed mock, so observePortrait's collector never engages
        // the (unmocked) network calls in submit/status/decode.
        val queueClient = mockk<PortraitQueueClient>(relaxed = true)
        return RandomNpcViewModel(
            context = context,
            completeNpcGenerator = completeNpcGenerator,
            npcDataGenerator = npcDataGenerator,
            temporaryRandomNpcRepository = repository,
            npcRepository = npcRepository,
            queueClient = queueClient,
            analytics = NoOpAnalytics
        )
    }

    test("randomizeCombatStats replaces the combat stats block held by the repository") {
        val repository = TemporaryRandomNpcRepository()
        repository.setNpc(sampleNpcData(combat = ORIGINAL_COMBAT))
        val completeNpcGenerator = mockk<CompleteNpcGenerator>()
        every { completeNpcGenerator.generateCombatStats() } returns REROLLED_COMBAT

        val viewModel = buildViewModel(repository, completeNpcGenerator)
        viewModel.randomizeCombatStats()

        viewModel.data.value.combat shouldBe REROLLED_COMBAT
        viewModel.data.value.combat shouldNotBe ORIGINAL_COMBAT
    }

    test("a combat-only re-roll does not change the built PortraitRequest, so no re-render would fire") {
        val repository = TemporaryRandomNpcRepository()
        repository.setNpc(sampleNpcData(combat = ORIGINAL_COMBAT))
        val completeNpcGenerator = mockk<CompleteNpcGenerator>()
        every { completeNpcGenerator.generateCombatStats() } returns REROLLED_COMBAT

        val viewModel = buildViewModel(repository, completeNpcGenerator)
        val requestBefore = PortraitPrompt.forNpc(viewModel.data.value.toPortraitNpc())

        viewModel.randomizeCombatStats()

        // Combat did change...
        viewModel.data.value.combat shouldBe REROLLED_COMBAT
        // ...but the prompt built from the (now combat-rerolled) NPC is identical: distinctUntilChanged
        // in observePortrait() would collapse this into a no-op, exactly like the architectural NOTE
        // on RandomNpcViewModel.toNpc() describes.
        val requestAfter = PortraitPrompt.forNpc(viewModel.data.value.toPortraitNpc())
        requestAfter shouldBe requestBefore
    }

    test("randomizeAll re-rolls every field when nothing is locked") {
        val repository = TemporaryRandomNpcRepository()
        repository.setNpc(CURRENT_DATA)
        val completeNpcGenerator = mockk<CompleteNpcGenerator> { every { generate() } returns FRESH_NPC }
        val context = mockk<Context>(relaxed = true) { every { getString(any()) } returns "generated" }

        val viewModel = buildViewModel(repository, completeNpcGenerator, context = context)
        viewModel.randomizeAll()

        val result = viewModel.data.value
        // Fields carried straight from the generated NPC.
        result.name shouldBe "FreshName"
        result.nickname shouldBe "FreshNick"
        result.profession shouldBe "FreshProfession"
        result.motivation shouldBe "FreshMotivation"
        result.personalityTraits shouldBe listOf("FreshTrait")
        result.combat shouldBe REROLLED_COMBAT
        // Enum-backed fields resolved through context.getString.
        result.gender shouldBe "generated"
        result.race shouldBe "generated"
        result.age shouldBe "generated"
        result.sexuality shouldBe "generated"
        result.alignment shouldBe "generated"
        result.languages shouldBe listOf("generated")
        // Nothing from the previous NPC leaked through.
        LockableField.values().forEach { field ->
            accessorFor(field)(result) shouldNotBe accessorFor(field)(CURRENT_DATA)
        }
    }

    test("each locked field keeps its current value through randomizeAll") {
        LockableField.values().forEach { field ->
            val repository = TemporaryRandomNpcRepository()
            repository.setNpc(CURRENT_DATA)
            val completeNpcGenerator = mockk<CompleteNpcGenerator> { every { generate() } returns FRESH_NPC }
            // A locked non-child age re-rolls the (unlocked) profession against the Adult pool.
            val npcDataGenerator = mockk<NpcDataGenerator> { every { generateProfession(any()) } returns "AdultJob" }

            val viewModel = buildViewModel(repository, completeNpcGenerator, npcDataGenerator)
            viewModel.toggleLock(field)
            viewModel.randomizeAll()

            withClue("locked $field") {
                accessorFor(field)(viewModel.data.value) shouldBe accessorFor(field)(CURRENT_DATA)
            }
        }
    }

    test("randomizeAll regenerates the profession for a locked child age") {
        val repository = TemporaryRandomNpcRepository()
        repository.setNpc(CURRENT_DATA.copy(age = "Child", profession = "OldChildJob"))
        val completeNpcGenerator = mockk<CompleteNpcGenerator> { every { generate() } returns FRESH_NPC }
        val npcDataGenerator = mockk<NpcDataGenerator> {
            every { generateProfession(Age.Child) } returns "Student"
            every { generateProfession(Age.Adult) } returns "Blacksmith"
        }
        val context = mockk<Context>(relaxed = true) { every { getString(Age.Child.nameResource) } returns "Child" }

        val viewModel = buildViewModel(repository, completeNpcGenerator, npcDataGenerator, context)
        viewModel.toggleLock(LockableField.AGE)
        viewModel.randomizeAll()

        val result = viewModel.data.value
        result.age shouldBe "Child"
        result.profession shouldBe "Student"
        verify(exactly = 1) { npcDataGenerator.generateProfession(Age.Child) }
        verify(exactly = 0) { npcDataGenerator.generateProfession(Age.Adult) }
    }

    test("randomizeAll regenerates the profession for a locked non-child age") {
        val repository = TemporaryRandomNpcRepository()
        repository.setNpc(CURRENT_DATA.copy(age = "Adult", profession = "OldAdultJob"))
        val completeNpcGenerator = mockk<CompleteNpcGenerator> { every { generate() } returns FRESH_NPC }
        val npcDataGenerator = mockk<NpcDataGenerator> {
            every { generateProfession(Age.Child) } returns "Student"
            every { generateProfession(Age.Adult) } returns "Blacksmith"
        }
        val context = mockk<Context>(relaxed = true) { every { getString(Age.Child.nameResource) } returns "Child" }

        val viewModel = buildViewModel(repository, completeNpcGenerator, npcDataGenerator, context)
        viewModel.toggleLock(LockableField.AGE)
        viewModel.randomizeAll()

        val result = viewModel.data.value
        result.age shouldBe "Adult"
        result.profession shouldBe "Blacksmith"
        verify(exactly = 1) { npcDataGenerator.generateProfession(Age.Adult) }
        verify(exactly = 0) { npcDataGenerator.generateProfession(Age.Child) }
    }

    test("randomizeAll keeps a locked profession and never regenerates it") {
        val repository = TemporaryRandomNpcRepository()
        repository.setNpc(CURRENT_DATA.copy(profession = "Locksmith"))
        val completeNpcGenerator = mockk<CompleteNpcGenerator> { every { generate() } returns FRESH_NPC }
        // Non-relaxed: any generateProfession call would throw, proving a locked profession is untouched.
        val npcDataGenerator = mockk<NpcDataGenerator>()

        val viewModel = buildViewModel(repository, completeNpcGenerator, npcDataGenerator)
        viewModel.toggleLock(LockableField.PROFESSION)
        viewModel.randomizeAll()

        viewModel.data.value.profession shouldBe "Locksmith"
        verify(exactly = 0) { npcDataGenerator.generateProfession(any()) }
    }

    test("randomizeAge does not re-roll a locked profession when the age group changes") {
        val repository = TemporaryRandomNpcRepository()
        repository.setNpc(CURRENT_DATA.copy(age = "Adult", profession = "Locksmith"))
        val completeNpcGenerator = mockk<CompleteNpcGenerator>() // randomizeAll is never called here
        val npcDataGenerator = mockk<NpcDataGenerator> { every { generateAge() } returns Age.Child }
        val context = mockk<Context>(relaxed = true) { every { getString(Age.Child.nameResource) } returns "Child" }

        val viewModel = buildViewModel(repository, completeNpcGenerator, npcDataGenerator, context)
        viewModel.toggleLock(LockableField.PROFESSION)
        viewModel.randomizeAge()

        viewModel.data.value.age shouldBe "Child"
        viewModel.data.value.profession shouldBe "Locksmith"
        verify(exactly = 0) { npcDataGenerator.generateProfession(any()) }
    }

    test("randomizeAge still re-rolls an unlocked profession when the age group changes") {
        val repository = TemporaryRandomNpcRepository()
        repository.setNpc(CURRENT_DATA.copy(age = "Adult", profession = "Locksmith"))
        val completeNpcGenerator = mockk<CompleteNpcGenerator>()
        val npcDataGenerator = mockk<NpcDataGenerator> {
            every { generateAge() } returns Age.Child
            // randomizeAge re-rolls the profession before setting the new age, so it reads the
            // still-current (Adult) group — preserving the app's existing behaviour.
            every { generateProfession(Age.Adult) } returns "Blacksmith"
        }
        val context = mockk<Context>(relaxed = true) { every { getString(Age.Child.nameResource) } returns "Child" }

        val viewModel = buildViewModel(repository, completeNpcGenerator, npcDataGenerator, context)
        viewModel.randomizeAge()

        viewModel.data.value.age shouldBe "Child"
        viewModel.data.value.profession shouldBe "Blacksmith"
        verify(exactly = 1) { npcDataGenerator.generateProfession(Age.Adult) }
    }

    test("randomizeAllItems replaces the items with a fresh roll for the current profession") {
        val repository = TemporaryRandomNpcRepository()
        repository.setNpc(sampleNpcData(combat = null, items = listOf("Old item")))
        val completeNpcGenerator = mockk<CompleteNpcGenerator>()
        every { completeNpcGenerator.generateItems("Blacksmith") } returns listOf("A set of smith's tools")

        val viewModel = buildViewModel(repository, completeNpcGenerator)
        viewModel.randomizeAllItems()

        viewModel.data.value.items shouldContainExactly listOf("A set of smith's tools")
        verify { completeNpcGenerator.generateItems("Blacksmith") }
    }

    test("randomizeItem re-rolls one row in place and appends when the index is past the end") {
        val repository = TemporaryRandomNpcRepository()
        repository.setNpc(sampleNpcData(combat = null, items = listOf("Old item")))
        val completeNpcGenerator = mockk<CompleteNpcGenerator>()
        every { completeNpcGenerator.generateSingleItem() } returns "A lucky copper coin"

        val viewModel = buildViewModel(repository, completeNpcGenerator)
        viewModel.randomizeItem(0)
        viewModel.data.value.items shouldContainExactly listOf("A lucky copper coin")

        viewModel.randomizeItem(1)
        viewModel.data.value.items shouldContainExactly listOf("A lucky copper coin", "A lucky copper coin")
    }

    test("an items-only re-roll does not change the built PortraitRequest, so no re-render would fire") {
        val repository = TemporaryRandomNpcRepository()
        repository.setNpc(sampleNpcData(combat = null, items = listOf("Old item")))
        val completeNpcGenerator = mockk<CompleteNpcGenerator>()
        every { completeNpcGenerator.generateItems(any()) } returns listOf("A completely different item")

        val viewModel = buildViewModel(repository, completeNpcGenerator)
        val requestBefore = PortraitPrompt.forNpc(viewModel.data.value.toPortraitNpc())

        viewModel.randomizeAllItems()

        // Items did change, but the prompt is identical: distinctUntilChanged collapses it to a no-op.
        viewModel.data.value.items shouldContainExactly listOf("A completely different item")
        PortraitPrompt.forNpc(viewModel.data.value.toPortraitNpc()) shouldBe requestBefore
    }
})
