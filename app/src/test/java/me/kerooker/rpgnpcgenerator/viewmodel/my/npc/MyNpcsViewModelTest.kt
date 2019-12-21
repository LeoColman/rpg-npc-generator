package me.kerooker.rpgnpcgenerator.viewmodel.my.npc

import androidx.lifecycle.MutableLiveData
import io.kotest.IsolationMode.InstancePerTest
import io.kotest.android.InstantLivedataListener
import io.kotest.shouldBe
import io.kotest.specs.ShouldSpec
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import me.kerooker.rpgnpcgenerator.repository.model.persistence.NpcEntity
import me.kerooker.rpgnpcgenerator.repository.model.persistence.NpcRepository

class MyNpcsViewModelTest : ShouldSpec() {
    
    private val repository: NpcRepository = mockk()
    
    private val target by lazy { MyNpcsViewModel(repository) }
    
    init {
        should("Start npcs to display as all values from repository") {
            val repositoryValues = listOf<NpcEntity>(mockk(), mockk())
            every { repository.all() } returns MutableLiveData(repositoryValues)
            
            target.npcsToDisplay.value shouldBe repositoryValues
        }
        
        should("Delete NPC from repository") {
            val npc = mockk<NpcEntity>()
    
            every { repository.delete(npc) } just Runs
            every { repository.all() } returns MutableLiveData(emptyList())
            
            target.deleteNpc(npc)
            
            verify { repository.delete(npc) }
        }
        
        should("Update current NPCS when deleting an NPC") {
            val npc = mockk<NpcEntity>()
            
            every { repository.delete(npc) } just Runs
            every { repository.all() } returns MutableLiveData(listOf(npc)) 
            
            target.deleteNpc(npc)
            
            verify { repository.delete(npc) }
        }
    }
    
    override fun listeners() = listOf(InstantLivedataListener())
    
    override fun isolationMode() = InstancePerTest
}
