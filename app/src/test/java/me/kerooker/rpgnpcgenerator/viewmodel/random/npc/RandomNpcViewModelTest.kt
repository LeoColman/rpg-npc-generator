package me.kerooker.rpgnpcgenerator.viewmodel.random.npc

import android.content.Context
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import me.kerooker.rpgnpcgenerator.analytics.NoOpAnalytics
import me.kerooker.rpgnpcgenerator.data.Npc
import me.kerooker.rpgnpcgenerator.data.NpcRepository
import me.kerooker.rpgnpcgenerator.repository.image.PortraitPrompt
import me.kerooker.rpgnpcgenerator.repository.image.PortraitQueueClient
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.CombatStats
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.CompleteNpcGenerator
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.GeneratedNpcData
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.NpcDataGenerator
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

private fun sampleNpcData(combat: CombatStats?) = GeneratedNpcData(
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
    combat = combat
)

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
    campaign = null
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
        completeNpcGenerator: CompleteNpcGenerator
    ): RandomNpcViewModel {
        val context = mockk<Context>(relaxed = true)
        val npcDataGenerator = mockk<NpcDataGenerator>()
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
})
