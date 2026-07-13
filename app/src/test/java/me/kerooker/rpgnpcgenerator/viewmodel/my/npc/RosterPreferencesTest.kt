package me.kerooker.rpgnpcgenerator.viewmodel.my.npc

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

private fun newPreferences(): RosterPreferences {
    val dir = Files.createTempDirectory("roster").toFile()
    val dataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.create { File(dir, "prefs.preferences_pb") }
    return RosterPreferences(dataStore)
}

/**
 * Exercises the real DataStore boundary of [RosterPreferences] under Robolectric. Each store gets a
 * fresh temp file so DataStore instances never clash.
 */
@RobolectricTest(sdk = [34], application = Application::class)
class RosterPreferencesTest : StringSpec({

    "defaults to name ascending" {
        newPreferences().sortOrder.first() shouldBe NpcSortOrder.NAME_ASC
    }

    "persists and reads back the chosen sort order" {
        val preferences = newPreferences()

        preferences.setSortOrder(NpcSortOrder.RECENTLY_ADDED)
        preferences.sortOrder.first() shouldBe NpcSortOrder.RECENTLY_ADDED

        preferences.setSortOrder(NpcSortOrder.NAME_DESC)
        preferences.sortOrder.first() shouldBe NpcSortOrder.NAME_DESC
    }
})
