package me.kerooker.rpgnpcgenerator.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback

/**
 * Loads and shows rewarded ads. Loading uses the application context; showing needs the (single)
 * Activity. Preload at app start and after every show/failure so a tap usually has an ad ready.
 */
class GmsRewardedAdController(
    private val appContext: Context,
    private val adIds: AdIds,
) : RewardedAdController {

    override val available = true

    private var rewardedAd: RewardedAd? = null
    private var loading = false

    override fun preload() {
        if (rewardedAd != null || loading) return
        loading = true
        RewardedAd.load(
            appContext,
            adIds.rewardedUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    loading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    loading = false
                }
            },
        )
    }

    override fun show(activity: Activity, onReward: () -> Unit, onUnavailable: () -> Unit) {
        val ad = rewardedAd
        if (ad == null) {
            onUnavailable()
            preload()
            return
        }
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() = preload()
            override fun onAdFailedToShowFullScreenContent(adError: AdError) = preload()
        }
        rewardedAd = null // consumed
        ad.show(activity) { onReward() }
    }
}
