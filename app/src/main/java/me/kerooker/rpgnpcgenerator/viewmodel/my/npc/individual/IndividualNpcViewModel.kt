package me.kerooker.rpgnpcgenerator.viewmodel.my.npc.individual

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.kerooker.rpgnpcgenerator.data.Npc
import me.kerooker.rpgnpcgenerator.data.NpcRepository
import me.kerooker.rpgnpcgenerator.repository.image.PortraitRepository

class IndividualNpcViewModel(
    private val npcId: Long,
    private val npcRepository: NpcRepository,
    private val portraitRepository: PortraitRepository
) : ViewModel() {

    val npc: StateFlow<Npc?> = npcRepository.get(npcId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS), null)

    private val _editState = MutableStateFlow(EditState.VIEW)
    val editState: StateFlow<EditState> = _editState.asStateFlow()

    private val _portraitState = MutableStateFlow<PortraitGenState>(PortraitGenState.Idle)
    val portraitState: StateFlow<PortraitGenState> = _portraitState.asStateFlow()

    // Emits the stored path of a freshly generated portrait so the editing draft can adopt it.
    private val _generatedPortraitPath = MutableSharedFlow<String>()
    val generatedPortraitPath: SharedFlow<String> = _generatedPortraitPath.asSharedFlow()

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

    fun generatePortrait(npc: Npc) {
        if (_portraitState.value == PortraitGenState.Generating) return
        _portraitState.value = PortraitGenState.Generating
        viewModelScope.launch {
            runCatching { portraitRepository.generateFor(npc) }
                .onSuccess { path ->
                    _generatedPortraitPath.emit(path)
                    _portraitState.value = PortraitGenState.Idle
                }
                .onFailure { _portraitState.value = PortraitGenState.Error(it.message) }
        }
    }

    fun clearPortraitError() {
        if (_portraitState.value is PortraitGenState.Error) _portraitState.value = PortraitGenState.Idle
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

enum class EditState {
    VIEW, EDIT
}

sealed interface PortraitGenState {
    data object Idle : PortraitGenState
    data object Generating : PortraitGenState
    data class Error(val message: String?) : PortraitGenState
}
