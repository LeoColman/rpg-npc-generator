package me.kerooker.rpgnpcgenerator.analytics

/**
 * Every analytics event the app captures, in one place. Events answer two product questions:
 * how much the generator + portrait pipeline are actually used (server capacity planning), and
 * whether users keep what they generate (saves) or pay attention to ads (reward grants).
 */
object AnalyticsEvents {

    /** A full re-roll on the Randomize screen (single-field re-rolls are not captured). */
    const val NPC_RANDOMIZED = "npc_randomized"

    /** A generated NPC was saved to the roster. Properties: [PROP_HAS_PORTRAIT]. */
    const val NPC_SAVED = "npc_saved"

    /** A portrait render was submitted to the server queue. Properties: [PROP_SOURCE]. */
    const val PORTRAIT_REQUESTED = "portrait_requested"

    /**
     * A portrait render finished and was shown/persisted. Properties: [PROP_SOURCE], and from the
     * Randomize screen also [PROP_QUEUE_AHEAD] + [PROP_WAIT_SECONDS] (client-observed queue health).
     */
    const val PORTRAIT_GENERATED = "portrait_generated"

    /** A portrait render failed (not cancelled). Properties: [PROP_SOURCE], [PROP_REASON]. */
    const val PORTRAIT_FAILED = "portrait_failed"

    /** The user watched a rewarded ad and earned the 7-day ad-free entitlement. */
    const val AD_FREE_WEEK_GRANTED = "ad_free_week_granted"

    /** Where a portrait render came from: the live Randomize screen or a saved NPC's worker. */
    const val PROP_SOURCE = "source"
    const val SOURCE_RANDOMIZE = "randomize"
    const val SOURCE_SAVED_NPC = "saved_npc"

    const val PROP_HAS_PORTRAIT = "has_portrait"
    const val PROP_QUEUE_AHEAD = "queue_ahead"
    const val PROP_WAIT_SECONDS = "wait_seconds"
    const val PROP_REASON = "reason"
}
