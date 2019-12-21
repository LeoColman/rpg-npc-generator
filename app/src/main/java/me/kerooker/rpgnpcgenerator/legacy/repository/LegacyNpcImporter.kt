package me.kerooker.rpgnpcgenerator.legacy.repository

import android.content.Context
import me.kerooker.rpgnpcgenerator.repository.model.persistence.NpcEntity
import me.kerooker.rpgnpcgenerator.repository.model.persistence.NpcRepository

class LegacyNpcImporter(
    private val legacyNpcRepository: LegacyNpcRepository,
    private val npcRepository: NpcRepository,
    private val context: Context
) {
    
    fun importAll() {
        val npcs = legacyNpcRepository.loadLegacyNpcs()
        
        if(npcs.isEmpty()) return
        
        npcs.forEach { 
            val newNpc = it.toNpcEntity()
            npcRepository.put(newNpc)
        }
        
        context.getSharedPreferences(SAVED_NPCS_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE).edit().clear().apply()
    }
    
    private fun LegacyNpc.toNpcEntity() = NpcEntity(
        this.name,
        "Nickname",
        this.gender,
        this.sexuality,
        this.race,
        this.age,
        this.profession,
        this.motivation,
        this.alignment,
        this.personalityTraits,
        this.languages,
        null
    )
    
}
