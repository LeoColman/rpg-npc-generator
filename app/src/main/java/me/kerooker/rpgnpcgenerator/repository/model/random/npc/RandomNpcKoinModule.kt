package me.kerooker.rpgnpcgenerator.repository.model.random.npc

import org.koin.dsl.module

val randomNpcModule = module {
    single { NameGenerator(get()) }
    single { NicknameGenerator(get()) }
    single { CommonProfessionGenerator(get()) }
    single { ChildProfessionGenerator(get()) }
    single { ProfessionGenerator(get(), get()) }
    single { MotivationGenerator(get()) }
    single { PersonalityTraitGenerator(get()) }
    
    single { NpcDataGenerator(get(), get(), get(), get(), get()) }
    single { CompleteNpcGenerator(get()) }
    
    single { TemporaryRandomNpcRepository() }
}
