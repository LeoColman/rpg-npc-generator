package me.kerooker.rpgnpcgenerator.ads

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Persists the "ads hidden until" epoch-millis deadline in DataStore and exposes it as reactive state.
 * A coarse [tickMillis] ticker is combined in so [adsEnabled] flips back on within ~a minute of the week
 * expiring even while the app stays open — a stored-value flow alone only re-emits on write.
 *
 * Boundary logic is delegated to [AdEntitlement] so it can be unit-tested independently. [clock] is
 * injectable for tests.
 */
class AdFreeStore(
    private val dataStore: DataStore<Preferences>,
    private val clock: () -> Long = { System.currentTimeMillis() },
    private val tickMillis: Long = TICK_MILLIS,
) {

    private val adFreeUntil: Flow<Long> = dataStore.data.map { it[KEY] ?: 0L }

    /** True while ads should be shown. */
    val adsEnabled: Flow<Boolean> =
        combine(adFreeUntil, ticker()) { until, _ -> AdEntitlement.adsEnabled(clock(), until) }

    /** Millis left in the ad-free window (0 once ads are active). Drives the remaining-time UI. */
    val adFreeRemainingMillis: Flow<Long> =
        combine(adFreeUntil, ticker()) { until, _ -> (until - clock()).coerceAtLeast(0L) }

    /** Grants (or extends) an ad-free week from the later of now / the current deadline. */
    suspend fun grantAdFreeWeek() {
        dataStore.edit { prefs ->
            prefs[KEY] = AdEntitlement.grantedUntil(clock(), prefs[KEY] ?: 0L, WEEK_MILLIS)
        }
    }

    private fun ticker(): Flow<Unit> = flow {
        while (true) {
            emit(Unit)
            delay(tickMillis)
        }
    }

    companion object {
        val KEY = longPreferencesKey("ad_free_until_epoch_millis")
        const val WEEK_MILLIS: Long = 7L * 24 * 60 * 60 * 1000
        const val TICK_MILLIS: Long = 60_000L
    }
}
