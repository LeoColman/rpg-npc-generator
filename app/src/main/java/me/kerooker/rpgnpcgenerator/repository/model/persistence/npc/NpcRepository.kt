package me.kerooker.rpgnpcgenerator.repository.model.persistence.npc

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.objectbox.Box
import io.objectbox.android.ObjectBoxLiveData
import io.objectbox.query.Query
import io.objectbox.reactive.DataObserver
import io.objectbox.reactive.DataSubscription


class NpcRepository(
    private val box: Box<NpcEntity>
) {
    
    fun all(): LiveData<List<NpcEntity>> {
        return ObjectBoxLiveData(box.query().build())
    }
    
    fun put(npcEntity: NpcEntity): Long {
        return box.put(npcEntity)
    }

    fun get(id: Long): MutableLiveData<NpcEntity> {
        return ObjectBoxSingleLiveData(
            box.query().equal(
                NpcEntity_.id,
                id
            ).build()
        )
    }
    
    fun delete(npcEntity: NpcEntity) {
        box.remove(npcEntity)
    }
}

class ObjectBoxSingleLiveData<T>(private val query: Query<T>) : MutableLiveData<T>() {
    private var subscription: DataSubscription? = null
    private val listener: DataObserver<List<T>> = DataObserver { data -> postValue(data.single()) }
    
    override fun onActive() {
        if (subscription == null) {
            subscription = query.subscribe().observer(listener)
        }
    }
    
    override fun onInactive() {
        if (!hasObservers()) {
            subscription!!.cancel()
            subscription = null
        }
    }
}
