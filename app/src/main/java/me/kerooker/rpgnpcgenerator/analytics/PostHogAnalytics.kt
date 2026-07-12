package me.kerooker.rpgnpcgenerator.analytics

import com.posthog.PostHog

/**
 * Forwards events to the PostHog singleton. [com.posthog.android.PostHogAndroid.setup] must have
 * run first — [analyticsModule] guarantees that by only binding this implementation after setup.
 */
class PostHogAnalytics : Analytics {

    override fun capture(event: String, properties: Map<String, Any>) {
        PostHog.capture(event = event, properties = properties)
    }
}
