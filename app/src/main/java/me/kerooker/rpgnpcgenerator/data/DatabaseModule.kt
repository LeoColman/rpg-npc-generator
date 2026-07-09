package me.kerooker.rpgnpcgenerator.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single<SqlDriver> {
        AndroidSqliteDriver(NpcDatabase.Schema, androidContext(), "npcs.db")
    }
    single {
        NpcDatabase(
            driver = get(),
            npcAdapter = Npc.Adapter(
                personalityTraitsAdapter = ListOfStringsAdapter,
                languagesAdapter = ListOfStringsAdapter
            )
        )
    }
    single { NpcRepository(get()) }
}
