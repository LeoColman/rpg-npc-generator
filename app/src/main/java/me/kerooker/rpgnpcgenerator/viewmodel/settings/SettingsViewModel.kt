package me.kerooker.rpgnpcgenerator.viewmodel.settings

import androidx.lifecycle.ViewModel
import me.kerooker.rpgnpcgenerator.viewmodel.admob.MyBuildConfig

class SettingsViewModel : ViewModel() {
    
    fun isPro() = "pro" in MyBuildConfig.APPLICATION_ID
}
