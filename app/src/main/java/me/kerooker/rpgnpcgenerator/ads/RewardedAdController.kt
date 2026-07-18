package me.kerooker.rpgnpcgenerator.ads

import android.app.Activity

/**
 * Loads and shows rewarded ads (used by the remove-ads-for-a-week reward). Only the `playstore`
 * flavor supplies a real GMS-backed implementation; ad-free flavors bind [NoOpRewardedAdController]
 * whose [available] is `false`, which also hides the remove-ads UI (see [RemoveAdsAction]).
 */
interface RewardedAdController {
    /** Whether rewarded ads exist in this build. Ad-free flavors report `false`. */
    val available: Boolean

    /** Loads an ad if none is ready. Idempotent. No-op when unavailable. */
    fun preload()

    /**
     * Shows a preloaded ad. [onReward] fires when the user earns the reward; [onUnavailable] fires
     * when nothing is loaded yet.
     */
    fun show(activity: Activity, onReward: () -> Unit, onUnavailable: () -> Unit)
}

/** Ad-free flavors (fdroid/github): no rewarded ads exist, so everything is inert. */
object NoOpRewardedAdController : RewardedAdController {
    override val available = false
    override fun preload() = Unit
    override fun show(activity: Activity, onReward: () -> Unit, onUnavailable: () -> Unit) = Unit
}
