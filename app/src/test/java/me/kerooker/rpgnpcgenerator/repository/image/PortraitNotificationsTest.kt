package me.kerooker.rpgnpcgenerator.repository.image

import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

private const val CHANNEL_ID = "portrait_gen"

/**
 * [PortraitNotifications] posts through `NotificationManagerCompat` against a real `Context` +
 * `NotificationManager`, so it's driven here via Robolectric (matching [PortraitQueueClient]'s test
 * in this same package).
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class PortraitNotificationsTest {

    private lateinit var context: Application
    private lateinit var notificationManager: NotificationManager
    private lateinit var notifications: PortraitNotifications

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        notificationManager = context.getSystemService(NotificationManager::class.java)
        notifications = PortraitNotifications(context)
    }

    @Test
    fun `ensureChannel creates the portrait_gen channel`() {
        notifications.ensureChannel()

        val channel = notificationManager.getNotificationChannel(CHANNEL_ID).shouldNotBeNull()

        channel.importance shouldBe NotificationManager.IMPORTANCE_DEFAULT
        channel.canShowBadge() shouldBe false
    }

    @Test
    fun `progress notification is ongoing with indeterminate progress`() {
        val notification = notifications.progress("Aria Nightsong", "Queued")

        (notification.flags and Notification.FLAG_ONGOING_EVENT != 0) shouldBe true
        notification.extras.getBoolean(Notification.EXTRA_PROGRESS_INDETERMINATE) shouldBe true
    }

    @Test
    fun `notifyProgress posts a notification under the npc id`() {
        notifications.ensureChannel()

        notifications.notifyProgress(42L, "Aria Nightsong", "Queued")

        shadowOf(notificationManager).getNotification(42).shouldNotBeNull()
    }

    @Test
    fun `notifyReady posts an auto-cancel notification under the npc id`() {
        notifications.ensureChannel()

        notifications.notifyReady(42L, "Aria Nightsong")

        val posted = shadowOf(notificationManager).getNotification(42).shouldNotBeNull()
        (posted.flags and Notification.FLAG_AUTO_CANCEL != 0) shouldBe true
    }

    @Test
    fun `notifyFailed posts an auto-cancel notification under the npc id`() {
        notifications.ensureChannel()

        notifications.notifyFailed(99L, "Aria Nightsong")

        val posted = shadowOf(notificationManager).getNotification(99).shouldNotBeNull()
        (posted.flags and Notification.FLAG_AUTO_CANCEL != 0) shouldBe true
    }

    @Test
    fun `safeNotify swallows failures instead of throwing`() {
        // A fully unstubbed Context makes every call inside safeNotify's runCatching block
        // (NotificationManagerCompat.from(context), building the notification, notify itself)
        // throw. The point of this test is that nothing escapes notifyProgress.
        val throwingContext = mockk<Context>()

        shouldNotThrowAny { PortraitNotifications(throwingContext).notifyProgress(1L, "Name", "Text") }
    }
}
