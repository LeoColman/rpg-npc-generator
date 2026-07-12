package me.kerooker.rpgnpcgenerator.ui.theme

/**
 * The three appearance modes the user can pick in Settings. [FOLLOW_SYSTEM] is the default so existing
 * users keep matching the OS and see no surprise on upgrade.
 *
 * [storedValue] is the stable string persisted in DataStore. It must never change once shipped, otherwise
 * an already-saved preference would silently fall back to [DEFAULT]. The enum<->string mapping lives here
 * (not in the store) so it can be unit-tested without Android or DataStore.
 */
enum class ThemePreference(val storedValue: String) {
    FOLLOW_SYSTEM("follow_system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        /** Applied when nothing is stored yet or a stored value is unrecognised. */
        val DEFAULT: ThemePreference = FOLLOW_SYSTEM

        /** Maps a persisted [storedValue] back to its enum, falling back to [DEFAULT] for null/unknown. */
        fun fromStoredValue(value: String?): ThemePreference =
            entries.firstOrNull { it.storedValue == value } ?: DEFAULT
    }
}
