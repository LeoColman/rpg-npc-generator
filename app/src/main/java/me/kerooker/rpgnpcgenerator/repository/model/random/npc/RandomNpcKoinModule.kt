package me.kerooker.rpgnpcgenerator.repository.model.random.npc

import me.kerooker.rpgnpcgenerator.R
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val randomNpcModule = module {
    single { NameGenerator(linesFromRaw(R.raw.npc_names, androidContext())) }
    single { NicknameGenerator(linesFromRaw(R.raw.npc_nicknames, androidContext())) }
    single { CommonProfessionGenerator(linesFromRaw(R.raw.npc_professions, androidContext())) }
    single { ChildProfessionGenerator(linesFromRaw(R.raw.npc_child_professions, androidContext())) }
    single { ProfessionGenerator(get(), get()) }
    single { MotivationGenerator(linesFromRaw(R.raw.npc_motivations, androidContext())) }
    single { PersonalityTraitGenerator(linesFromRaw(R.raw.npc_personality_trait, androidContext())) }

    single { NpcDataGenerator(get(), get(), get(), get(), get()) }
    single { CombatStatsGenerator() }
    single { CompleteNpcGenerator(get(), get()) }

    single { TemporaryRandomNpcRepository() }
}
