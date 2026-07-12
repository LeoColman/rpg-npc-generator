package me.kerooker.rpgnpcgenerator.viewmodel

import me.kerooker.rpgnpcgenerator.viewmodel.my.npc.MyNpcsViewModel
import me.kerooker.rpgnpcgenerator.viewmodel.my.npc.individual.IndividualNpcViewModel
import me.kerooker.rpgnpcgenerator.viewmodel.random.npc.RandomNpcViewModel
import me.kerooker.rpgnpcgenerator.viewmodel.settings.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {
    viewModel { RandomNpcViewModel(get(), get(), get(), get(), get(), get(), get()) }
    viewModel { MyNpcsViewModel(get(), get()) }
    viewModel { (npcId: Long) -> IndividualNpcViewModel(npcId, get(), androidContext()) }
    viewModel { SettingsViewModel(get(), androidContext(), get()) }
}
