package me.kerooker.rpgnpcgenerator.repository.model.persistence

import android.content.Context
import io.objectbox.BoxStore
import io.objectbox.android.AndroidObjectBrowser
import me.kerooker.rpgnpcgenerator.BuildConfig
import me.kerooker.rpgnpcgenerator.repository.model.persistence.admob.AdmobRepository
import me.kerooker.rpgnpcgenerator.repository.model.persistence.npc.MyObjectBox
import me.kerooker.rpgnpcgenerator.repository.model.persistence.npc.NpcEntity
import me.kerooker.rpgnpcgenerator.repository.model.persistence.npc.NpcRepository
import org.koin.core.scope.Scope
import org.koin.dsl.module

val persistenceModule = module {
    single(createdAtStart = true) { createObjectBox() }
    single { NpcRepository(get<BoxStore>().boxFor(NpcEntity::class.java)) }
    single { AdmobRepository(get()) }
}

private fun Scope.createObjectBox(): BoxStore {
    val store = MyObjectBox.builder().androidContext(get<Context>()).build()

    if(BuildConfig.DEBUG) {
        AndroidObjectBrowser(store).start(get())
    }
    return store
}
