package me.kerooker.rpgnpcgenerator.ui.theme

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File
import java.nio.file.Files

/**
 * Exercises the real DataStore boundary of [ThemePreferenceStore] under Robolectric (mirrors
 * AdFreeStoreTest). Each store gets a fresh temp file so DataStore instances never clash.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class ThemePreferenceStoreTest {

    private fun newStore(): ThemePreferenceStore {
        val dir = Files.createTempDirectory("theme").toFile()
        val dataStore: DataStore<Preferences> =
            PreferenceDataStoreFactory.create { File(dir, "prefs.preferences_pb") }
        return ThemePreferenceStore(dataStore)
    }

    @Test
    fun `defaults to follow system when nothing is stored`() = runBlocking {
        newStore().themePreference.first() shouldBe ThemePreference.FOLLOW_SYSTEM
        Unit
    }

    @Test
    fun `persists and reads back each preference`() = runBlocking {
        val store = newStore()

        store.setThemePreference(ThemePreference.DARK)
        store.themePreference.first() shouldBe ThemePreference.DARK

        store.setThemePreference(ThemePreference.LIGHT)
        store.themePreference.first() shouldBe ThemePreference.LIGHT

        store.setThemePreference(ThemePreference.FOLLOW_SYSTEM)
        store.themePreference.first() shouldBe ThemePreference.FOLLOW_SYSTEM
        Unit
    }
}
