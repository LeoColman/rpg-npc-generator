package me.kerooker.rpgnpcgenerator.viewmodel

import me.kerooker.rpgnpcgenerator.viewmodel.admob.AdmobViewModel
import me.kerooker.rpgnpcgenerator.viewmodel.my.npc.MyNpcsViewModel
import me.kerooker.rpgnpcgenerator.viewmodel.my.npc.individual.IndividualNpcViewModel
import me.kerooker.rpgnpcgenerator.viewmodel.random.npc.RandomNpcViewModel
import me.kerooker.rpgnpcgenerator.viewmodel.settings.SettingsViewModel
import org.koin.android.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelsModule = module {
    viewModel { RandomNpcViewModel(get(), get(), get(), get(), get()) }
    viewModel { MyNpcsViewModel(get()) }
    viewModel { (npcId: Long) -> IndividualNpcViewModel(npcId, get()) }
    viewModel { AdmobViewModel(get()) }
    viewModel { SettingsViewModel() }
}
