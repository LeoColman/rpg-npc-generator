package me.kerooker.rpgnpcgenerator.viewmodel.my.npc

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import me.kerooker.rpgnpcgenerator.data.Npc

private fun npc(
    id: Long,
    fullName: String = "Name $id",
    nickname: String = "",
    profession: String = "",
    race: String = "",
    campaign: String? = null
) = Npc(
    id = id,
    fullName = fullName,
    nickname = nickname,
    gender = "",
    sexuality = "",
    race = race,
    age = "",
    profession = profession,
    motivation = "",
    alignment = "",
    personalityTraits = emptyList(),
    languages = emptyList(),
    imagePath = null,
    notes = "",
    campaign = campaign
)

class MyNpcsRosterTest : FunSpec({

    context("npcMatchesQuery") {
        val target =
            npc(1, fullName = "Aria Nightsong", nickname = "The Swift", profession = "Ranger", race = "High Elf")

        test("blank query matches everything") {
            npcMatchesQuery(target, "") shouldBe true
            npcMatchesQuery(target, "   ") shouldBe true
        }

        test("matches any of name, nickname, profession, race, case-insensitively") {
            npcMatchesQuery(target, "aria") shouldBe true
            npcMatchesQuery(target, "SWIFT") shouldBe true
            npcMatchesQuery(target, "rang") shouldBe true
            npcMatchesQuery(target, "elf") shouldBe true
        }

        test("does not match unrelated text or non-searched fields") {
            npcMatchesQuery(target, "wizard") shouldBe false
            // motivation/alignment are not part of the search surface
            npcMatchesQuery(npc(2, fullName = "Bob", campaign = "Waterdeep"), "waterdeep") shouldBe false
        }
    }

    context("distinctCampaigns") {
        test("dedupes, trims, drops blanks and sorts case-insensitively") {
            val npcs = listOf(
                npc(1, campaign = "Waterdeep"),
                npc(2, campaign = " Waterdeep "),
                npc(3, campaign = "avernus"),
                npc(4, campaign = null),
                npc(5, campaign = "   ")
            )
            distinctCampaigns(npcs) shouldContainExactly listOf("avernus", "Waterdeep")
        }
    }

    context("sortNpcs") {
        val npcs = listOf(
            npc(1, fullName = "banjo"),
            npc(3, fullName = "Apple"),
            npc(2, fullName = "cactus")
        )

        test("NAME_ASC orders alphabetically, ignoring case") {
            sortNpcs(npcs, NpcSortOrder.NAME_ASC).map { it.fullName } shouldContainExactly
                listOf("Apple", "banjo", "cactus")
        }

        test("RECENTLY_ADDED orders by id descending") {
            sortNpcs(npcs, NpcSortOrder.RECENTLY_ADDED).map { it.id } shouldContainExactly listOf(3L, 2L, 1L)
        }
    }

    context("groupByCampaign") {
        test("named campaigns first (A-Z), unassigned last, empty groups omitted") {
            val npcs = listOf(
                npc(1, campaign = "Waterdeep"),
                npc(2, campaign = null),
                npc(3, campaign = "avernus"),
                npc(4, campaign = "Waterdeep")
            )
            val sections = groupByCampaign(npcs)
            sections.map { it.campaign } shouldContainExactly listOf("avernus", "Waterdeep", null)
            sections[1].npcs.map { it.id } shouldContainExactly listOf(1L, 4L)
        }
    }

    context("buildRoster") {
        val npcs = listOf(
            npc(1, fullName = "Aria", profession = "Ranger", campaign = "Waterdeep"),
            npc(2, fullName = "Bran", profession = "Smith", campaign = "Avernus"),
            npc(3, fullName = "Cora", profession = "Ranger", campaign = "Waterdeep"),
            npc(4, fullName = "Dain", profession = "Mage", campaign = null)
        )

        test("no filter, ungrouped: single section sorted by name, all campaigns available") {
            val state = buildRoster(npcs, RosterFilter())
            state.sections.size shouldBe 1
            state.sections.single().campaign shouldBe null
            state.sections.single().npcs.map { it.fullName } shouldContainExactly listOf("Aria", "Bran", "Cora", "Dain")
            state.availableCampaigns shouldContainExactly listOf("Avernus", "Waterdeep")
            state.totalCount shouldBe 4
            state.hasNpcs shouldBe true
            state.hasResults shouldBe true
        }

        test("campaign filter keeps only that campaign's npcs") {
            val state = buildRoster(npcs, RosterFilter(campaign = "Waterdeep"))
            state.sections.single().npcs.map { it.fullName } shouldContainExactly listOf("Aria", "Cora")
        }

        test("search query filters across fields and stacks with campaign filter") {
            val state = buildRoster(npcs, RosterFilter(query = "ranger", campaign = "Waterdeep"))
            state.sections.single().npcs.map { it.fullName } shouldContainExactly listOf("Aria", "Cora")
        }

        test("recently-added sort orders by id descending") {
            val state = buildRoster(npcs, RosterFilter(sortOrder = NpcSortOrder.RECENTLY_ADDED))
            state.sections.single().npcs.map { it.id } shouldContainExactly listOf(4L, 3L, 2L, 1L)
        }

        test("grouping splits into campaign sections with unassigned last") {
            val state = buildRoster(npcs, RosterFilter(groupByCampaign = true))
            state.sections.map { it.campaign } shouldContainExactly listOf("Avernus", "Waterdeep", null)
        }

        test("a query that matches nothing yields no results but still reports saved npcs") {
            val state = buildRoster(npcs, RosterFilter(query = "nonexistent"))
            state.hasResults shouldBe false
            state.hasNpcs shouldBe true
        }

        test("empty roster reports no npcs") {
            val state = buildRoster(emptyList(), RosterFilter())
            state.hasNpcs shouldBe false
            state.hasResults shouldBe false
            state.totalCount shouldBe 0
        }
    }
})
