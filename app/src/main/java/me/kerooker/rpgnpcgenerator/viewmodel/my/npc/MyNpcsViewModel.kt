package me.kerooker.rpgnpcgenerator.viewmodel.my.npc


import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import me.kerooker.rpgnpcgenerator.repository.model.persistence.NpcEntity
import me.kerooker.rpgnpcgenerator.repository.model.persistence.NpcRepository

class MyNpcsViewModel(
    private val npcRepository: NpcRepository
) : ViewModel() {
    
    val npcsToDisplay: LiveData<List<NpcEntity>> = npcRepository.all()
    
    fun deleteNpc(npc: NpcEntity) {
        npcRepository.delete(npc)
    }
}
