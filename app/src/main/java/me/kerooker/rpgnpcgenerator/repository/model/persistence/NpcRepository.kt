package me.kerooker.rpgnpcgenerator.repository.model.persistence

import io.objectbox.Box

interface NpcRepository {

    fun all(): List<NpcEntity>
    
    fun put(npcEntity: NpcEntity): Long

    fun get(id: Long): NpcEntity
}

class NpcBoxRepository(
    private val box: Box<NpcEntity>
) : NpcRepository {
    
    override fun all(): List<NpcEntity> {
        return box.all
    }
    
    override fun put(npcEntity: NpcEntity): Long {
        return box.put(npcEntity)
    }

    override fun get(id: Long): NpcEntity {
        return box.get(id)
    }
}
