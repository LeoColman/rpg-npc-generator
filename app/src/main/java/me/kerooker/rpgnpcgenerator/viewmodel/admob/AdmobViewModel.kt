package me.kerooker.rpgnpcgenerator.viewmodel.admob

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import me.kerooker.rpgnpcgenerator.isFirebaseDevice
import me.kerooker.rpgnpcgenerator.repository.model.persistence.admob.AdmobRepository
import java.util.Date

@Suppress("ReturnCount")
class AdmobViewModel(
    private val admobRepository: AdmobRepository
) : ViewModel() {
    
    val bannerAdId = 
        if("debug" in MyBuildConfig.APPLICATION_ID) "ca-app-pub-3940256099942544/6300978111" 
        else "ca-app-pub-4066886200642192/4591525789"
    
    val rewardedAdId =
        if("debug" in MyBuildConfig.APPLICATION_ID) "ca-app-pub-3940256099942544/5224354917"
        else "ca-app-pub-4066886200642192/4016810716"
    
    private val _shouldShowAd by lazy { MutableLiveData(calculateShouldShowAd()) }
    val shouldShowAd: LiveData<Boolean> by lazy { _shouldShowAd }
    
    private fun calculateShouldShowAd(): Boolean {
        if(isFirebaseDevice) return false
        if ("pro" in MyBuildConfig.APPLICATION_ID) return false
        if(admobRepository.stopAdsUntil > Date().time) return false
        return true
    }
    
    fun shouldSuggestRemovingAds(): Boolean {
        if(isFirebaseDevice) return false
        if(!calculateShouldShowAd()) return false
        if(admobRepository.stopSuggestingRemovalUntil > Date().time) return false
        return true
    }
    
    fun suggestedRemovingAds() {
        val now = Date()
        admobRepository.stopSuggestingRemovalUntil = now.time + TEN_MINUTES_MILLIS
    }
    
    fun watchedRewardedAd() {
        val now = Date()
        admobRepository.stopSuggestingRemovalUntil = now.time + ONE_DAY_MILLIS
        admobRepository.stopAdsUntil = now.time + ONE_DAY_MILLIS
        _shouldShowAd.value = calculateShouldShowAd()
    }
    
}

private const val ONE_DAY_MILLIS = 24 * 60 * 60 * 1000
private const val TEN_MINUTES_MILLIS = 10 * 60 * 1000

