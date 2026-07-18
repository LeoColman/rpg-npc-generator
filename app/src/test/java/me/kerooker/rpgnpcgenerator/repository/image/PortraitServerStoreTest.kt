package me.kerooker.rpgnpcgenerator.repository.image

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import java.io.File
import java.nio.file.Files

private val DEFAULTS = RemoteImageConfig(
    baseUrl = "https://default.example",
    username = "default-user",
    password = "default-pass"
)

private fun newStore(defaults: RemoteImageConfig = DEFAULTS): PortraitServerStore {
    val dir = Files.createTempDirectory("portrait").toFile()
    val dataStore: DataStore<Preferences> =
        PreferenceDataStoreFactory.create { File(dir, "prefs.preferences_pb") }
    return PortraitServerStore(dataStore, defaults)
}

/**
 * Exercises the real DataStore boundary of [PortraitServerStore] under Robolectric. Each store gets a
 * fresh temp file so DataStore instances never clash.
 */
@RobolectricTest(sdk = [34], application = Application::class)
class PortraitServerStoreTest : StringSpec({

    "defaults to the baked config when nothing is stored" {
        newStore().current() shouldBe DEFAULTS
    }

    "save overrides every field" {
        val store = newStore()

        store.save("https://mine.example", "me", "secret")

        store.current() shouldBe RemoteImageConfig("https://mine.example", "me", "secret")
    }

    "a stored blank password wins over the default, disabling portraits" {
        val store = newStore()

        store.save("https://mine.example", "me", "")

        val config = store.current()
        config.password shouldBe ""
        config.enabled shouldBe false
    }

    "reset reverts to the baked defaults" {
        val store = newStore()
        store.save("https://mine.example", "me", "secret")

        store.reset()

        store.current() shouldBe DEFAULTS
    }
})
