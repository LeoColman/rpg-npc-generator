package me.kerooker.rpgnpcgenerator.repository.image

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import me.kerooker.rpgnpcgenerator.R

/** Progress + completion notifications for background portrait generation. One per NPC id. */
class PortraitNotifications(private val context: Context) {

    fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = context.getString(R.string.portrait_notification_channel_name)
            val channel = NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            channel.setShowBadge(false)
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    /** Ongoing, silent notification for queue/generation progress. Caller updates it as state changes. */
    fun progress(name: String, text: String): Notification =
        base(name)
            .setContentText(text)
            .setProgress(0, 0, true)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .build()

    fun notifyProgress(npcId: Long, name: String, text: String) {
        safeNotify(npcId) { it.notify(npcId.toInt(), progress(name, text)) }
    }

    fun notifyReady(npcId: Long, name: String) {
        safeNotify(npcId) {
            it.notify(
                npcId.toInt(),
                base(name).setContentText(context.getString(R.string.portrait_notification_ready))
                    .setAutoCancel(true)
                    .build()
            )
        }
    }

    fun notifyFailed(npcId: Long, name: String) {
        safeNotify(npcId) {
            it.notify(
                npcId.toInt(),
                base(name).setContentText(context.getString(R.string.portrait_notification_failed))
                    .setAutoCancel(true)
                    .build()
            )
        }
    }

    private fun base(name: String): NotificationCompat.Builder =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_twenty_sided_dice)
            .setContentTitle(name)
            .setContentIntent(openAppIntent())
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)

    private fun openAppIntent(): PendingIntent {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            ?.apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }
        return PendingIntent.getActivity(
            context, 0, intent ?: Intent(),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    // POST_NOTIFICATIONS may be denied on API 33+; posting then throws SecurityException — ignore,
    // the portrait still lands in the DB and appears when the user reopens the NPC.
    private inline fun safeNotify(npcId: Long, block: (NotificationManagerCompat) -> Unit) {
        runCatching { block(NotificationManagerCompat.from(context)) }
    }

    private companion object {
        const val CHANNEL_ID = "portrait_gen"
    }
}
