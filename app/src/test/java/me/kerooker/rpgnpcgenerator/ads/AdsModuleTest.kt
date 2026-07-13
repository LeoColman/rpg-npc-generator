package me.kerooker.rpgnpcgenerator.ads

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.test.core.app.ApplicationProvider
import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import me.kerooker.rpgnpcgenerator.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.koinApplication

/**
 * Verifies [adsModule] wiring in an isolated Koin container (mirrors ImageGenModuleTest). One test
 * method builds the graph once so only a single DataStore instance is created for the file.
 */
@RobolectricTest(sdk = [34], application = Application::class)
class AdsModuleTest : StringSpec({

    "adsModule resolves every dependency, feeds AdIds from BuildConfig, and shares singletons" {
        val context: Application = ApplicationProvider.getApplicationContext()
        val koin = koinApplication {
            androidContext(context)
            modules(adsModule)
        }.koin
        try {
            koin.get<DataStore<Preferences>>()
            koin.get<ConsentManager>()
            koin.get<RewardedAdController>()

            val adIds = koin.get<AdIds>()
            adIds.bannerUnitId shouldBe BuildConfig.ADMOB_BANNER_UNIT_ID
            adIds.rewardedUnitId shouldBe BuildConfig.ADMOB_REWARDED_UNIT_ID

            koin.get<AdFreeStore>() shouldBeSameInstanceAs koin.get<AdFreeStore>()
        } finally {
            koin.close()
        }
    }
})
