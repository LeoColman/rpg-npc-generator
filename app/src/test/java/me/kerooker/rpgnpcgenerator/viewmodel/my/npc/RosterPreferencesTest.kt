package me.kerooker.rpgnpcgenerator.viewmodel.my.npc

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
 * Exercises the real DataStore boundary of [RosterPreferences] under Robolectric. Each store gets a
 * fresh temp file so DataStore instances never clash.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class RosterPreferencesTest {

    private fun newPreferences(): RosterPreferences {
        val dir = Files.createTempDirectory("roster").toFile()
        val dataStore: DataStore<Preferences> =
            PreferenceDataStoreFactory.create { File(dir, "prefs.preferences_pb") }
        return RosterPreferences(dataStore)
    }

    @Test
    fun `defaults to name ascending`() = runBlocking {
        newPreferences().sortOrder.first() shouldBe NpcSortOrder.NAME_ASC
        Unit
    }

    @Test
    fun `persists and reads back the chosen sort order`() = runBlocking {
        val preferences = newPreferences()

        preferences.setSortOrder(NpcSortOrder.RECENTLY_ADDED)
        preferences.sortOrder.first() shouldBe NpcSortOrder.RECENTLY_ADDED

        preferences.setSortOrder(NpcSortOrder.NAME_DESC)
        preferences.sortOrder.first() shouldBe NpcSortOrder.NAME_DESC
        Unit
    }
}
