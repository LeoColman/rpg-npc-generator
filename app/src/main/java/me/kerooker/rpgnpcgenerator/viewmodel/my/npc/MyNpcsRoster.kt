package me.kerooker.rpgnpcgenerator.viewmodel.my.npc

import me.kerooker.rpgnpcgenerator.data.Npc

/** How the saved-NPC list is ordered. */
enum class NpcSortOrder {
    /** Alphabetical by full name (A–Z), case-insensitive. */
    NAME_ASC,

    /** Most recently added first (highest id first). */
    RECENTLY_ADDED
}

/**
 * The user's current roster view options. Held in the view model and applied to the persisted
 * NPC list by [buildRoster]. A `null` [campaign] means "all campaigns".
 */
data class RosterFilter(
    val query: String = "",
    val campaign: String? = null,
    val sortOrder: NpcSortOrder = NpcSortOrder.NAME_ASC,
    val groupByCampaign: Boolean = false
)

/**
 * A rendered slice of the roster. When grouping is off there is a single section with a `null`
 * [campaign]; when grouping is on, [campaign] is the header label, or `null` for the trailing
 * "no campaign" section.
 */
data class RosterSection(
    val campaign: String?,
    val npcs: List<Npc>
)

/** Everything the list screen needs to render, derived purely from the NPCs plus the [filter]. */
data class RosterUiState(
    val sections: List<RosterSection>,
    val availableCampaigns: List<String>,
    val filter: RosterFilter,
    val totalCount: Int
) {
    /** True when the user has saved at least one NPC (regardless of the active filter). */
    val hasNpcs: Boolean get() = totalCount > 0

    /** True when the active filter matched at least one NPC. */
    val hasResults: Boolean get() = sections.any { it.npcs.isNotEmpty() }
}

/** Normalizes a stored campaign value to its display form, or `null` when blank/unset. */
fun Npc.campaignOrNull(): String? = campaign?.trim()?.takeIf { it.isNotEmpty() }

/**
 * Case-insensitive match of [query] against name, nickname, profession or race. A blank query
 * matches everything.
 */
fun npcMatchesQuery(npc: Npc, query: String): Boolean {
    val needle = query.trim().lowercase()
    if (needle.isEmpty()) return true
    return npc.fullName.lowercase().contains(needle) ||
        npc.nickname.lowercase().contains(needle) ||
        npc.profession.lowercase().contains(needle) ||
        npc.race.lowercase().contains(needle)
}

/** Distinct, non-blank campaign names present in [npcs], ordered case-insensitively. */
fun distinctCampaigns(npcs: List<Npc>): List<String> =
    npcs.mapNotNull { it.campaignOrNull() }
        .distinct()
        .sortedBy { it.lowercase() }

/** Orders [npcs] according to [order]. Pure; returns a new list. */
fun sortNpcs(npcs: List<Npc>, order: NpcSortOrder): List<Npc> = when (order) {
    NpcSortOrder.NAME_ASC -> npcs.sortedBy { it.fullName.lowercase() }
    NpcSortOrder.RECENTLY_ADDED -> npcs.sortedByDescending { it.id }
}

/**
 * Splits [npcs] (already filtered and sorted) into campaign sections: named campaigns first
 * (case-insensitive A–Z), then a trailing `null` section for NPCs with no campaign. Sections
 * with no members are omitted.
 */
fun groupByCampaign(npcs: List<Npc>): List<RosterSection> {
    val byCampaign = npcs.groupBy { it.campaignOrNull() }
    val named = byCampaign.keys.filterNotNull()
        .sortedBy { it.lowercase() }
        .map { RosterSection(it, byCampaign.getValue(it)) }
    val unassigned = byCampaign[null]?.let { listOf(RosterSection(null, it)) }.orEmpty()
    return named + unassigned
}

/**
 * The single entry point the view model uses: applies the campaign filter, the search query, the
 * sort order and optional grouping to [npcs], and reports the distinct campaigns available for the
 * filter UI. Pure and fully unit-testable.
 */
fun buildRoster(npcs: List<Npc>, filter: RosterFilter): RosterUiState {
    val byCampaign = if (filter.campaign == null) {
        npcs
    } else {
        npcs.filter { it.campaignOrNull() == filter.campaign }
    }
    val filtered = byCampaign.filter { npcMatchesQuery(it, filter.query) }
    val sorted = sortNpcs(filtered, filter.sortOrder)
    val sections = if (filter.groupByCampaign) {
        groupByCampaign(sorted)
    } else {
        listOf(RosterSection(null, sorted))
    }
    return RosterUiState(
        sections = sections,
        availableCampaigns = distinctCampaigns(npcs),
        filter = filter,
        totalCount = npcs.size
    )
}
