package me.kerooker.rpgnpcgenerator.analytics

import com.posthog.PostHog

/**
 * Forwards events to the PostHog singleton. [com.posthog.android.PostHogAndroid.setup] must have
 * run first — [createAnalytics] guarantees that by only returning this implementation after setup.
 * telemetry source set (github/playstore) only.
 */
class PostHogAnalytics : Analytics {

    override fun capture(event: String, properties: Map<String, Any>) {
        PostHog.capture(event = event, properties = properties)
    }
}
