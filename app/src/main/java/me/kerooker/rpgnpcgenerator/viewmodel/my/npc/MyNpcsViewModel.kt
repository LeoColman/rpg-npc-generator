package me.kerooker.rpgnpcgenerator.viewmodel.my.npc

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.kerooker.rpgnpcgenerator.data.Npc
import me.kerooker.rpgnpcgenerator.data.NpcRepository

class MyNpcsViewModel(
    private val npcRepository: NpcRepository
) : ViewModel() {

    val npcsToDisplay: StateFlow<List<Npc>> = npcRepository.all()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), emptyList())

    fun deleteNpc(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            npcRepository.delete(id)
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
