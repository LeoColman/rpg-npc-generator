package me.kerooker.rpgnpcgenerator.analytics

/**
 * Product analytics abstraction. Call sites capture named events (see [AnalyticsEvents]) and stay
 * unaware of the backing provider, so debug builds (and any build without an API key) can swap in
 * [NoOpAnalytics] and send nothing at all.
 */
interface Analytics {
    fun capture(event: String, properties: Map<String, Any> = emptyMap())
}

/** Used whenever no API key is configured (all debug builds): analytics is fully disabled. */
object NoOpAnalytics : Analytics {
    override fun capture(event: String, properties: Map<String, Any>) = Unit
}
