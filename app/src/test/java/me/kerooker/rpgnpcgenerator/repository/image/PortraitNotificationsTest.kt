package me.kerooker.rpgnpcgenerator.repository.image

import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.robolectric.Shadows.shadowOf

private const val CHANNEL_ID = "portrait_gen"

/**
 * [PortraitNotifications] posts through `NotificationManagerCompat` against a real `Context` +
 * `NotificationManager`, so it's driven here via Robolectric (matching [PortraitQueueClient]'s test
 * in this same package).
 */
@RobolectricTest(sdk = [34], application = Application::class)
class PortraitNotificationsTest : StringSpec({

    lateinit var context: Application
    lateinit var notificationManager: NotificationManager
    lateinit var notifications: PortraitNotifications

    beforeTest {
        context = ApplicationProvider.getApplicationContext()
        notificationManager = context.getSystemService(NotificationManager::class.java)
        notifications = PortraitNotifications(context)
    }

    "ensureChannel creates the portrait_gen channel" {
        notifications.ensureChannel()

        val channel = notificationManager.getNotificationChannel(CHANNEL_ID).shouldNotBeNull()

        channel.importance shouldBe NotificationManager.IMPORTANCE_DEFAULT
        channel.canShowBadge() shouldBe false
    }

    "progress notification is ongoing with indeterminate progress" {
        val notification = notifications.progress("Aria Nightsong", "Queued")

        (notification.flags and Notification.FLAG_ONGOING_EVENT != 0) shouldBe true
        notification.extras.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE) shouldBe true
    }

    "notifyProgress posts a notification under the npc id" {
        notifications.ensureChannel()

        notifications.notifyProgress(42L, "Aria Nightsong", "Queued")

        shadowOf(notificationManager).getNotification(42).shouldNotBeNull()
    }

    "notifyReady posts an auto-cancel notification under the npc id" {
        notifications.ensureChannel()

        notifications.notifyReady(42L, "Aria Nightsong")

        val posted = shadowOf(notificationManager).getNotification(42).shouldNotBeNull()
        (posted.flags and Notification.FLAG_AUTO_CANCEL != 0) shouldBe true
    }

    "notifyFailed posts an auto-cancel notification under the npc id" {
        notifications.ensureChannel()

        notifications.notifyFailed(99L, "Aria Nightsong")

        val posted = shadowOf(notificationManager).getNotification(99).shouldNotBeNull()
        (posted.flags and Notification.FLAG_AUTO_CANCEL != 0) shouldBe true
    }

    "safeNotify swallows failures instead of throwing" {
        // A fully unstubbed Context makes every call inside safeNotify's runCatching block
        // (NotificationManagerCompat.from(context), building the notification, notify itself)
        // throw. The point of this test is that nothing escapes notifyProgress.
        val throwingContext = mockk<Context>()

        shouldNotThrowAny { PortraitNotifications(throwingContext).notifyProgress(1L, "Name", "Text") }
    }
})
