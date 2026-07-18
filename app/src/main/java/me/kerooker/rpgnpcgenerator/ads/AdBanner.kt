package me.kerooker.rpgnpcgenerator.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.koinInject

/**
 * Bottom-anchored banner slot. Renders nothing while the user's ad-free week is active, so it can be
 * placed unconditionally in the app scaffold. The actual banner is drawn by the injected
 * [BannerAdRenderer]: the real GMS `AdView` on `playstore`, and nothing at all on the ad-free flavors
 * (fdroid/github), which bind [NoOpBannerAdRenderer].
 */
@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    val adFreeStore = koinInject<AdFreeStore>()
    // Start hidden so ad-free users never see a banner flash before the first emission.
    val adsEnabled by adFreeStore.adsEnabled.collectAsStateWithLifecycle(initialValue = false)
    if (!adsEnabled) return

    koinInject<BannerAdRenderer>().Content(modifier)
}
