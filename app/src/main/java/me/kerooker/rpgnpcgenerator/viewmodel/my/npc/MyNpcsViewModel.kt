package me.kerooker.rpgnpcgenerator.viewmodel.my.npc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.kerooker.rpgnpcgenerator.data.NpcRepository

class MyNpcsViewModel(
    private val npcRepository: NpcRepository,
    private val rosterPreferences: RosterPreferences
) : ViewModel() {

    // Query, campaign, tag and grouping live here; the sort order is sourced from DataStore
    // ([rosterPreferences]) so it survives process death, and is merged in when building the roster.
    private val viewOptions = MutableStateFlow(RosterFilter())

    /** Everything the screen renders: the (filtered, sorted, optionally grouped) NPCs plus the
     * campaigns and tags available to filter by and the active filter. Recomputed whenever the
     * persisted NPCs, their tags, the view options or the persisted sort order change. */
    val uiState: StateFlow<RosterUiState> =
        combine(
            npcRepository.all(),
            npcRepository.allTags(),
            viewOptions,
            rosterPreferences.sortOrder
        ) { npcs, tags, options, sortOrder ->
            buildRoster(npcs, options.copy(sortOrder = sortOrder), tags)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            RosterUiState(sections = emptyList(), availableCampaigns = emptyList(), filter = RosterFilter(), totalCount = 0)
        )

    fun setQuery(query: String) = viewOptions.update { it.copy(query = query) }

    fun setCampaignFilter(campaign: String?) = viewOptions.update { it.copy(campaign = campaign) }

    fun setTagFilter(tag: String?) = viewOptions.update { it.copy(tag = tag) }

    /** Persists the chosen sort order; the state pipeline re-derives from the DataStore emission. */
    fun setSortOrder(order: NpcSortOrder) {
        viewModelScope.launch { rosterPreferences.setSortOrder(order) }
    }

    fun setGroupByCampaign(grouped: Boolean) = viewOptions.update { it.copy(groupByCampaign = grouped) }

    fun deleteNpc(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            npcRepository.delete(id)
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
