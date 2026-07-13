package me.kerooker.rpgnpcgenerator.ui.theme

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

private fun newStore(): ThemePreferenceStore {
    val dir = Files.createTempDirectory("theme").toFile()
    val dataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.create { File(dir, "prefs.preferences_pb") }
    return ThemePreferenceStore(dataStore)
}

/**
 * Exercises the real DataStore boundary of [ThemePreferenceStore] under Robolectric (mirrors
 * AdFreeStoreTest). Each store gets a fresh temp file so DataStore instances never clash.
 */
@RobolectricTest(sdk = [34], application = Application::class)
class ThemePreferenceStoreTest : StringSpec({

    "defaults to follow system when nothing is stored" {
        newStore().themePreference.first() shouldBe ThemePreference.FOLLOW_SYSTEM
    }

    "persists and reads back each preference" {
        val store = newStore()

        store.setThemePreference(ThemePreference.DARK)
        store.themePreference.first() shouldBe ThemePreference.DARK

        store.setThemePreference(ThemePreference.LIGHT)
        store.themePreference.first() shouldBe ThemePreference.LIGHT

        store.setThemePreference(ThemePreference.FOLLOW_SYSTEM)
        store.themePreference.first() shouldBe ThemePreference.FOLLOW_SYSTEM
    }
})
