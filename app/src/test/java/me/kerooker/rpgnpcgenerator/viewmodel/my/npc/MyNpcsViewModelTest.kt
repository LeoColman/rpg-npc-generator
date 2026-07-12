package me.kerooker.rpgnpcgenerator.viewmodel.my.npc

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import me.kerooker.rpgnpcgenerator.data.Npc
import me.kerooker.rpgnpcgenerator.data.NpcRepository

private fun npc(id: Long, fullName: String, campaign: String? = null) = Npc(
    id = id,
    fullName = fullName,
    nickname = "",
    gender = "",
    sexuality = "",
    race = "",
    age = "",
    profession = "",
    motivation = "",
    alignment = "",
    personalityTraits = emptyList(),
    languages = emptyList(),
    imagePath = null,
    notes = "",
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
    items = emptyList()
)

// stateIn(WhileSubscribed) replays its initial empty seed to a new subscriber before the combined
// upstream produces the real state; depending on dispatcher timing that seed may or may not be
// conflated away. Await forward until the predicate holds so the tests don't depend on that timing.
private suspend fun ReceiveTurbine<RosterUiState>.awaitState(
    predicate: (RosterUiState) -> Boolean
): RosterUiState {
    while (true) {
        val item = awaitItem()
        if (predicate(item)) return item
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class MyNpcsViewModelTest : FunSpec({

    beforeSpec { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterSpec { Dispatchers.resetMain() }

    fun viewModelWith(npcs: List<Npc>, tags: Map<Long, List<String>> = emptyMap()): MyNpcsViewModel {
        val repository = mockk<NpcRepository>()
        every { repository.all() } returns MutableStateFlow(npcs)
        every { repository.allTags() } returns MutableStateFlow(tags)
        val preferences = mockk<RosterPreferences>()
        every { preferences.sortOrder } returns flowOf(NpcSortOrder.NAME_ASC)
        coEvery { preferences.setSortOrder(any()) } returns Unit
        return MyNpcsViewModel(repository, preferences)
    }

    test("uiState reflects the persisted npcs, sorted by name by default") {
        val viewModel = viewModelWith(listOf(npc(1, "Zara"), npc(2, "Alba")))

        viewModel.uiState.test {
            val state = awaitState { it.hasNpcs }
            state.sections.single().npcs.map { it.fullName } shouldContainExactly listOf("Alba", "Zara")
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("setQuery narrows the derived roster") {
        val viewModel = viewModelWith(listOf(npc(1, "Zara"), npc(2, "Alba")))

        viewModel.uiState.test {
            awaitState { it.hasResults }.sections.single().npcs.size shouldBe 2

            viewModel.setQuery("alb")
            val filtered = awaitState { it.filter.query == "alb" }
            filtered.sections.single().npcs.map { it.fullName } shouldContainExactly listOf("Alba")

            cancelAndIgnoreRemainingEvents()
        }
    }

    test("setCampaignFilter and setGroupByCampaign flow through to the derived state") {
        val viewModel = viewModelWith(
            listOf(npc(1, "Zara", campaign = "Avernus"), npc(2, "Alba", campaign = "Waterdeep"))
        )

        viewModel.uiState.test {
            awaitState { it.hasNpcs }.availableCampaigns shouldContainExactly listOf("Avernus", "Waterdeep")

            viewModel.setCampaignFilter("Waterdeep")
            awaitState { it.filter.campaign == "Waterdeep" }
                .sections.single().npcs.map { it.fullName } shouldContainExactly listOf("Alba")

            viewModel.setCampaignFilter(null)
            viewModel.setGroupByCampaign(true)
            val grouped = awaitState { it.filter.groupByCampaign }
            grouped.filter.campaign shouldBe null
            grouped.sections.map { it.campaign } shouldContainExactly listOf("Avernus", "Waterdeep")

            cancelAndIgnoreRemainingEvents()
        }
    }

    test("tags surface in state and both the query and the tag filter narrow by them") {
        val viewModel = viewModelWith(
            npcs = listOf(npc(1, "Zara"), npc(2, "Alba")),
            tags = mapOf(1L to listOf("Villain"), 2L to listOf("Ally"))
        )

        viewModel.uiState.test {
            val initial = awaitState { it.hasNpcs }
            initial.availableTags shouldContainExactly listOf("Ally", "Villain")
            initial.tagsByNpc[1L] shouldContainExactly listOf("Villain")

            viewModel.setQuery("villain")
            awaitState { it.filter.query == "villain" }
                .sections.single().npcs.map { it.fullName } shouldContainExactly listOf("Zara")

            viewModel.setQuery("")
            viewModel.setTagFilter("Ally")
            awaitState { it.filter.tag == "Ally" }
                .sections.single().npcs.map { it.fullName } shouldContainExactly listOf("Alba")

            cancelAndIgnoreRemainingEvents()
        }
    }
})
