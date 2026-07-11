package me.kerooker.rpgnpcgenerator.ads

import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import org.koin.compose.koinInject

private const val MAX_RETRIES = 4
private const val BASE_RETRY_DELAY_MS = 2_000L
private const val MAX_RETRY_DELAY_MS = 30_000L

/**
 * Bottom anchored adaptive banner. Renders nothing while the user's ad-free week is active, so it can
 * be placed unconditionally in the app scaffold. Retries a few times with exponential backoff so a
 * transient load failure doesn't leave the slot permanently blank.
 */
@Composable
fun AdBanner(modifier: Modifier = Modifier) {
    val adFreeStore = koinInject<AdFreeStore>()
    // Start hidden so ad-free users never see a banner flash before the first emission.
    val adsEnabled by adFreeStore.adsEnabled.collectAsStateWithLifecycle(initialValue = false)
    if (!adsEnabled) return

    val adIds = koinInject<AdIds>()
    val context = LocalContext.current
    val widthDp = LocalConfiguration.current.screenWidthDp

    val adView = remember {
        AdView(context).apply {
            adUnitId = adIds.bannerUnitId
            setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp))
        }
    }
    DisposableEffect(adView) {
        val handler = Handler(Looper.getMainLooper())
        var retries = 0
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                retries = 0
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
                if (retries >= MAX_RETRIES) return
                retries++
                val delay = (BASE_RETRY_DELAY_MS shl (retries - 1)).coerceAtMost(MAX_RETRY_DELAY_MS)
                handler.postDelayed({ adView.loadAd(AdRequest.Builder().build()) }, delay)
            }
        }
        adView.loadAd(AdRequest.Builder().build())
        onDispose {
            handler.removeCallbacksAndMessages(null)
            adView.destroy()
        }
    }
    AndroidView(factory = { adView }, modifier = modifier.fillMaxWidth())
}
