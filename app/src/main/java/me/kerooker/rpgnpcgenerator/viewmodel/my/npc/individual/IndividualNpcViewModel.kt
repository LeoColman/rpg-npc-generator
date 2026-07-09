package me.kerooker.rpgnpcgenerator.viewmodel.my.npc.individual

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.kerooker.rpgnpcgenerator.data.Npc
import me.kerooker.rpgnpcgenerator.data.NpcRepository
import me.kerooker.rpgnpcgenerator.repository.image.GeneratePortraitWorker

class IndividualNpcViewModel(
    private val npcId: Long,
    private val npcRepository: NpcRepository,
    private val appContext: Context
) : ViewModel() {

    val npc: StateFlow<Npc?> = npcRepository.get(npcId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)

    private val _editState = MutableStateFlow(EditState.VIEW)
    val editState: StateFlow<EditState> = _editState.asStateFlow()

    fun enableEdit() {
        _editState.value = EditState.EDIT
    }

    fun cancelEdit() {
        _editState.value = EditState.VIEW
    }

    fun saveEdit(edited: Npc) {
        _editState.value = EditState.VIEW
        viewModelScope.launch(Dispatchers.IO) {
            npcRepository.update(edited.copy(id = npcId))
        }
    }

    fun delete() {
        viewModelScope.launch(Dispatchers.IO) {
            npcRepository.delete(npcId)
        }
    }

    /**
     * Fire-and-forget: queues a background portrait render for this NPC. The worker submits to the
     * server queue, notifies on completion, and writes the image back to the NPC — which this
     * screen picks up automatically via [npc]. Safe to leave the screen after calling.
     */
    fun generatePortrait() {
        GeneratePortraitWorker.enqueue(appContext, npcId)
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

enum class EditState {
    VIEW, EDIT
}
