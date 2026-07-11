package me.kerooker.rpgnpcgenerator.ads

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import me.kerooker.rpgnpcgenerator.BuildConfig
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.koinApplication
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Verifies [adsModule] wiring in an isolated Koin container (mirrors ImageGenModuleTest). One test
 * method builds the graph once so only a single DataStore instance is created for the file.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class AdsModuleTest {

    private val context: Application = ApplicationProvider.getApplicationContext()

    @Test
    fun `adsModule resolves every dependency, feeds AdIds from BuildConfig, and shares singletons`() {
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
}
