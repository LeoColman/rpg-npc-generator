package me.kerooker.rpgnpcgenerator.crash

import android.content.Context
import io.sentry.android.core.SentryAndroid
import io.sentry.protocol.User
import me.kerooker.rpgnpcgenerator.BuildConfig

/**
 * Crash reporting against the self-hosted GlitchTip instance (Sentry protocol) on ritalee — see
 * `server/glitchtip/`. Initialized first thing in [me.kerooker.rpgnpcgenerator.RpgNpcGeneratorApplication]
 * so a crash during startup (the ones that hurt most) is still captured.
 *
 * Auto-init via the manifest ContentProvider is switched off so this is the single entry point:
 * a blank DSN (every debug build) means the SDK is never started and nothing leaves the device.
 */
object CrashReporting {

    fun init(context: Context, dsn: String = BuildConfig.GLITCHTIP_DSN) {
        if (dsn.isBlank()) return
        SentryAndroid.init(context) { options ->
            options.dsn = dsn
            options.release = "${BuildConfig.APPLICATION_ID}@${BuildConfig.VERSION_NAME}"
            options.environment = if (BuildConfig.DEBUG) "debug" else "production"
            // SDK-internal logging (what got captured, what got sent) — only when verifying a debug
            // build against a DSN; release builds stay silent.
            options.isDebug = BuildConfig.DEBUG

            // Diagnostics only: no personal data, no NPC content. The user is anonymous — GlitchTip
            // would otherwise attach the device IP as an identifier.
            options.isSendDefaultPii = false
            options.beforeSend = io.sentry.SentryOptions.BeforeSendCallback { event, _ ->
                event.user = ANONYMOUS_USER
                event
            }

            // Breadcrumbs are recorded but kept coarse: no view/click text, no user input.
            options.isEnableUserInteractionBreadcrumbs = false
            options.isAttachScreenshot = false
            options.isAttachViewHierarchy = false

            // Report every crash: with one app's volume there is nothing to sample away.
            options.sampleRate = 1.0
            options.isEnableAutoSessionTracking = true
        }
    }

    private val ANONYMOUS_USER = User().apply { ipAddress = null }

    /**
     * Throws an uncaught exception so a developer can confirm, after any change to the reporting
     * setup, that crashes really do reach GlitchTip. Debug builds only — the Settings entry that
     * calls it is compiled behind the same [BuildConfig.DEBUG] check, so it can't reach users.
     */
    fun forceTestCrash(): Nothing = error("Test crash from Settings — verifying GlitchTip reporting")
}
