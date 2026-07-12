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
    // Item flavour tables live in code (see ItemsGenerator); the locale is resolved here — once, the
    // same lifetime a FileGenerator loads its locale's raw file — so item strings match the app language.
    single { ItemsGenerator(portuguese = androidContext().isPortugueseLocale()) }
    single { CompleteNpcGenerator(get(), get(), get()) }

    single { TemporaryRandomNpcRepository() }
}

/** True when the device's primary locale is Portuguese, so item rolls should emit their pt variants. */
private fun android.content.Context.isPortugueseLocale(): Boolean =
    resources.configuration.locales[0].language == "pt"
