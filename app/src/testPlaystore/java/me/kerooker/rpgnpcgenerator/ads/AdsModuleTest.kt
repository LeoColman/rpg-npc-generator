package me.kerooker.rpgnpcgenerator.ads

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.test.core.app.ApplicationProvider
import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import me.kerooker.rpgnpcgenerator.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.koinApplication

/**
 * Verifies the playstore ads wiring: the base [adsModule] no-ops overridden by [adsRealModule] with
 * the real GMS implementations, AdIds fed from BuildConfig. playstore-flavor test only — the FOSS
 * fdroid/github builds never compile the real ads code.
 */
@RobolectricTest(sdk = [34], application = Application::class)
class AdsModuleTest : StringSpec({

    "adsModule + adsRealModule resolve every dependency, feed AdIds from BuildConfig, and share singletons" {
        val context: Application = ApplicationProvider.getApplicationContext()
        val koin = koinApplication {
            androidContext(context)
            modules(adsModule, adsRealModule)
        }.koin
        try {
            koin.get<DataStore<Preferences>>()
            koin.get<ConsentManager>().shouldBeInstanceOf<GmsConsentManager>()
            koin.get<RewardedAdController>().available shouldBe true
            koin.get<BannerAdRenderer>() shouldBeSameInstanceAs GmsBannerAdRenderer

            val adIds = koin.get<AdIds>()
            adIds.bannerUnitId shouldBe BuildConfig.ADMOB_BANNER_UNIT_ID
            adIds.rewardedUnitId shouldBe BuildConfig.ADMOB_REWARDED_UNIT_ID

            koin.get<AdFreeStore>() shouldBeSameInstanceAs koin.get<AdFreeStore>()
        } finally {
            koin.close()
        }
    }
})
