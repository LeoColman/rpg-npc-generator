package me.kerooker.rpgnpcgenerator.ui.theme

import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.shouldBe

/** Pure mapping between [ThemePreference] and the string persisted in DataStore. */
class ThemePreferenceTest : FunSpec({

    test("every preference round-trips through its stored value") {
        ThemePreference.entries.forAll { preference ->
            ThemePreference.fromStoredValue(preference.storedValue) shouldBe preference
        }
    }

    test("stored values are the exact, stable strings (must not change once shipped)") {
        ThemePreference.FOLLOW_SYSTEM.storedValue shouldBe "follow_system"
        ThemePreference.LIGHT.storedValue shouldBe "light"
        ThemePreference.DARK.storedValue shouldBe "dark"
    }

    test("an unknown stored value falls back to the default (follow system)") {
        ThemePreference.fromStoredValue("nonsense") shouldBe ThemePreference.FOLLOW_SYSTEM
        ThemePreference.DEFAULT shouldBe ThemePreference.FOLLOW_SYSTEM
    }

    test("a missing (null) stored value falls back to the default") {
        ThemePreference.fromStoredValue(null) shouldBe ThemePreference.DEFAULT
    }
})
