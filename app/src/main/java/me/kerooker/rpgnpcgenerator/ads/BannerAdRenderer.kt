package me.kerooker.rpgnpcgenerator.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Renders the bottom banner. Injected into [AdBanner] so the ad-free flavors (fdroid/github) can bind
 * [NoOpBannerAdRenderer] (draws nothing) while `playstore` binds the real GMS `AdView` renderer — no
 * proprietary ads code is compiled into the FOSS build.
 */
interface BannerAdRenderer {
    @Composable
    fun Content(modifier: Modifier)
}

/** Ad-free flavors (fdroid/github): the banner slot renders nothing. */
object NoOpBannerAdRenderer : BannerAdRenderer {
    @Composable
    override fun Content(modifier: Modifier) = Unit
}
