package me.kerooker.rpgnpcgenerator.viewmodel.my.npc

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Persists the user's chosen roster sort order in DataStore and exposes it as reactive state, so the
 * My NPCs list reopens with the same ordering. Mirrors the DataStore pattern used by AdFreeStore.
 * An unknown/absent stored value falls back to [NpcSortOrder.NAME_ASC].
 */
class RosterPreferences(
    private val dataStore: DataStore<Preferences>
) {

    /** The persisted sort order, defaulting to A–Z until the user picks otherwise. */
    val sortOrder: Flow<NpcSortOrder> = dataStore.data.map { prefs ->
        prefs[SORT_ORDER_KEY]?.let { stored ->
            runCatching { NpcSortOrder.valueOf(stored) }.getOrNull()
        } ?: NpcSortOrder.NAME_ASC
    }

    suspend fun setSortOrder(order: NpcSortOrder) {
        dataStore.edit { prefs -> prefs[SORT_ORDER_KEY] = order.name }
    }

    companion object {
        val SORT_ORDER_KEY = stringPreferencesKey("npc_sort_order")
    }
}
