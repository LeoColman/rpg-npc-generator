package me.kerooker.rpgnpcgenerator.ads

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import java.io.File
import java.nio.file.Files

private fun newStore(clock: () -> Long): AdFreeStore {
    val dir = Files.createTempDirectory("adfree").toFile()
    val dataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.create { File(dir, "prefs.preferences_pb") }
    return AdFreeStore(dataStore, clock = clock, tickMillis = 10L)
}

/**
 * Exercises the real DataStore + clock boundary of [AdFreeStore] under Robolectric. Each store gets a
 * fresh temp file so DataStore instances never clash, and a fake clock drives the week boundary.
 */
@RobolectricTest(sdk = [34], application = Application::class)
class AdFreeStoreTest : StringSpec({

    "defaults to ads enabled with no ad-free time" {
        val store = newStore(clock = { 1_000L })
        store.adsEnabled.first() shouldBe true
        store.adFreeRemainingMillis.first() shouldBe 0L
    }

    "granting a week hides ads and reports a full week remaining" {
        val now = 1_000_000L
        val store = newStore(clock = { now })

        store.grantAdFreeWeek()

        store.adsEnabled.first() shouldBe false
        store.adFreeRemainingMillis.first() shouldBe AdFreeStore.WEEK_MILLIS
    }

    "ads return once the granted week has elapsed" {
        var now = 1_000_000L
        val store = newStore(clock = { now })
        store.grantAdFreeWeek()
        store.adsEnabled.first() shouldBe false

        now += AdFreeStore.WEEK_MILLIS + 1

        store.adsEnabled.first() shouldBe true
        store.adFreeRemainingMillis.first() shouldBe 0L
    }
})
