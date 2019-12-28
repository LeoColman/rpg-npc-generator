package me.kerooker.rpgnpcgenerator.repository.model.persistence.admob

import android.content.Context
import com.tencent.mmkv.MMKV
import java.util.Date

class AdmobRepository(
    context: Context
) {
    
    init {
        MMKV.initialize(context)
    }
    
    private val kv = MMKV.defaultMMKV() 
    
    private val now = Date().time
    var stopSuggestingRemovalUntil: Long
        get() =  kv.getLong("stopSuggestingRemovalUntil", now + FIVE_SECONDS_MILLIS)
        set(value) {
            kv.putLong("stopSuggestingRemovalUntil", value)
        }
    
    var stopAdsUntil: Long
        get() = kv.getLong("stopAdsUntil", Date().time)
        set(value) {
            kv.putLong("stopAdsUntil", value)
        }
}

private const val FIVE_SECONDS_MILLIS = 5_000
