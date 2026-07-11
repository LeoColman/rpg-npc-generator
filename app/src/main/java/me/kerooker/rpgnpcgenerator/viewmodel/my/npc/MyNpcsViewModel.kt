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
    private val npcRepository: NpcRepository
) : ViewModel() {

    private val filter = MutableStateFlow(RosterFilter())

    /** Everything the screen renders: the (filtered, sorted, optionally grouped) NPCs plus the
     * campaigns available to filter by and the active filter. Recomputed whenever the persisted
     * NPCs or the filter change. */
    val uiState: StateFlow<RosterUiState> =
        combine(npcRepository.all(), filter) { npcs, activeFilter ->
            buildRoster(npcs, activeFilter)
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            RosterUiState(sections = emptyList(), availableCampaigns = emptyList(), filter = filter.value, totalCount = 0)
        )

    fun setQuery(query: String) = filter.update { it.copy(query = query) }

    fun setCampaignFilter(campaign: String?) = filter.update { it.copy(campaign = campaign) }

    fun setSortOrder(order: NpcSortOrder) = filter.update { it.copy(sortOrder = order) }

    fun setGroupByCampaign(grouped: Boolean) = filter.update { it.copy(groupByCampaign = grouped) }

    fun deleteNpc(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            npcRepository.delete(id)
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
