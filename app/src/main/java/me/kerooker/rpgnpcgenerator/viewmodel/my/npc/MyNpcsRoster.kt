package me.kerooker.rpgnpcgenerator.viewmodel.my.npc

import me.kerooker.rpgnpcgenerator.data.Npc
import java.text.Normalizer

/** How the saved-NPC list is ordered. */
enum class NpcSortOrder {
    /** Alphabetical by full name (A–Z), case-insensitive. */
    NAME_ASC,

    /** Reverse alphabetical by full name (Z–A), case-insensitive. */
    NAME_DESC,

    /** Most recently added first (highest id first). */
    RECENTLY_ADDED
}

/**
 * The user's current roster view options. Held in the view model and applied to the persisted
 * NPC list by [buildRoster]. A `null` [campaign] means "all campaigns"; a `null` [tag] means
 * "all tags".
 */
data class RosterFilter(
    val query: String = "",
    val campaign: String? = null,
    val tag: String? = null,
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
    val totalCount: Int,
    /** Tags per NPC id, so the list can render each NPC's tag chips without another lookup. */
    val tagsByNpc: Map<Long, List<String>> = emptyMap(),
    /** Distinct tags in use across the whole roster, ordered case-insensitively. */
    val availableTags: List<String> = emptyList()
) {
    /** True when the user has saved at least one NPC (regardless of the active filter). */
    val hasNpcs: Boolean get() = totalCount > 0

    /** True when the active filter matched at least one NPC. */
    val hasResults: Boolean get() = sections.any { it.npcs.isNotEmpty() }
}

/** Normalizes a stored campaign value to its display form, or `null` when blank/unset. */
fun Npc.campaignOrNull(): String? = campaign?.trim()?.takeIf { it.isNotEmpty() }

/**
 * Folds [text] to a case- and diacritic-insensitive form for searching, so "josé" matches "jose"
 * and "AÇÃO" matches "acao". Trims, lowercases, then strips Unicode combining marks.
 */
fun normalizeForSearch(text: String): String =
    Normalizer.normalize(text.trim().lowercase(), Normalizer.Form.NFD)
        .replace(COMBINING_MARKS, "")

private val COMBINING_MARKS = Regex("\\p{Mn}+")

/**
 * Case- and diacritic-insensitive match of [query] against name, nickname, profession, race or any
 * of the NPC's [tags]. A blank query matches everything.
 */
fun npcMatchesQuery(npc: Npc, query: String, tags: List<String> = emptyList()): Boolean {
    val needle = normalizeForSearch(query)
    if (needle.isEmpty()) return true
    return normalizeForSearch(npc.fullName).contains(needle) ||
        normalizeForSearch(npc.nickname).contains(needle) ||
        normalizeForSearch(npc.profession).contains(needle) ||
        normalizeForSearch(npc.race).contains(needle) ||
        tags.any { normalizeForSearch(it).contains(needle) }
}

/** Distinct, non-blank campaign names present in [npcs], ordered case-insensitively. */
fun distinctCampaigns(npcs: List<Npc>): List<String> =
    npcs.mapNotNull { it.campaignOrNull() }
        .distinct()
        .sortedBy { it.lowercase() }

/** Distinct, non-blank tags present in [tagsByNpc], ordered case-insensitively. */
fun distinctTags(tagsByNpc: Map<Long, List<String>>): List<String> =
    tagsByNpc.values.flatten()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinctBy { it.lowercase() }
        .sortedBy { it.lowercase() }

/**
 * Cleans a raw list of tags for persistence: trims each, drops blanks, and de-duplicates
 * case-insensitively while keeping the first spelling and the original order.
 */
fun sanitizeTags(tags: List<String>): List<String> =
    tags.map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinctBy { it.lowercase() }

/** Orders [npcs] according to [order]. Pure; returns a new list. */
fun sortNpcs(npcs: List<Npc>, order: NpcSortOrder): List<Npc> = when (order) {
    NpcSortOrder.NAME_ASC -> npcs.sortedBy { it.fullName.lowercase() }
    NpcSortOrder.NAME_DESC -> npcs.sortedByDescending { it.fullName.lowercase() }
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
 * The single entry point the view model uses: applies the campaign filter, the tag filter, the
 * search query, the sort order and optional grouping to [npcs], and reports the distinct campaigns
 * and tags available for the filter UI. [tagsByNpc] maps each NPC id to its tags. Pure and fully
 * unit-testable.
 */
fun buildRoster(
    npcs: List<Npc>,
    filter: RosterFilter,
    tagsByNpc: Map<Long, List<String>> = emptyMap()
): RosterUiState {
    val byCampaign = if (filter.campaign == null) {
        npcs
    } else {
        npcs.filter { it.campaignOrNull() == filter.campaign }
    }
    val byTag = if (filter.tag == null) {
        byCampaign
    } else {
        byCampaign.filter { npc -> tagsByNpc[npc.id].orEmpty().any { it.equals(filter.tag, ignoreCase = true) } }
    }
    val filtered = byTag.filter { npcMatchesQuery(it, filter.query, tagsByNpc[it.id].orEmpty()) }
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
        totalCount = npcs.size,
        tagsByNpc = tagsByNpc,
        availableTags = distinctTags(tagsByNpc)
    )
}
