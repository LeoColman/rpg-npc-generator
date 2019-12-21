package me.kerooker.rpgnpcgenerator.viewmodel.my.npc.individual

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.kerooker.rpgnpcgenerator.repository.model.persistence.NpcEntity
import me.kerooker.rpgnpcgenerator.repository.model.persistence.NpcRepository

class IndividualNpcViewModel(
    npcId: Long,
    private val npcRepository: NpcRepository
) : ViewModel() {
    
    private val _npc: MutableLiveData<NpcEntity> = npcRepository.get(npcId)
    val npc: LiveData<NpcEntity> = _npc
    
    private val _editState = MutableLiveData(EditState.VIEW)
    val editState: LiveData<EditState> = _editState
    
    fun enableEdit() {
        _editState.value = EditState.EDIT
    }
    
    fun saveEdit(npcEntity: NpcEntity) {
        _editState.value = EditState.VIEW
        val current = npc.value!!
        val new = npcEntity.copy(id = current.id)
        npcRepository.put(new)
        
    }
    
    fun cancelEdit() {
        _editState.value = EditState.VIEW
        _npc.value = _npc.value
    }
}

enum class EditState {
    VIEW, EDIT
}
