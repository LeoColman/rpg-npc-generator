package me.kerooker.rpgnpcgenerator.repository.image

import android.content.Context
import android.graphics.BitmapFactory
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.firstOrNull
import me.kerooker.rpgnpcgenerator.R
import me.kerooker.rpgnpcgenerator.data.NpcRepository
import me.kerooker.rpgnpcgenerator.ui.util.ImageStore
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.IOException

/**
 * Generates an NPC portrait in the background so the user can leave and come back. Submits to the
 * server queue (on ritalee), polls for position/result (surfacing progress in a notification), then
 * writes the image to the NPC in the DB and posts a "ready" notification. Rendering is server-only:
 * if the server is unreachable or not configured, the job fails rather than producing a local image.
 * The portrait appears automatically when the DB row updates.
 */
class GeneratePortraitWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {

    private val npcRepository: NpcRepository by inject()
    private val queueClient: PortraitQueueClient by inject()
    private val notifications: PortraitNotifications by inject()

    // Keeps the render alive when the app is backgrounded. On API < 31 expedited work runs as a
    // short-lived foreground service using this notification; on API 31+ it's an expedited job.
    override suspend fun getForegroundInfo(): ForegroundInfo {
        notifications.ensureChannel()
        val npcId = inputData.getLong(KEY_NPC_ID, -1L)
        val id = if (npcId >= 0) npcId.toInt() else FOREGROUND_FALLBACK_ID
        val notification = notifications.progress(
            str(R.string.individual_npc_generate_portrait),
            str(R.string.portrait_notification_queued)
        )
        return ForegroundInfo(id, notification)
    }

    override suspend fun doWork(): Result {
        val npcId = inputData.getLong(KEY_NPC_ID, -1L)
        val npc = if (npcId >= 0) npcRepository.get(npcId).firstOrNull() else null
        if (npc == null) return Result.failure()
        val request = PortraitPrompt.forNpc(npc)

        notifications.ensureChannel()
        notifications.notifyProgress(npcId, npc.fullName, str(R.string.portrait_notification_queued))

        val path = if (!queueClient.enabled) {
            null
        } else {
            runCatching { renderRemote(npcId, npc.fullName, request) }.getOrNull()
        }
        if (path == null) {
            notifications.notifyFailed(npcId, npc.fullName)
            return Result.failure()
        }

        val previous = npc.imagePath
        npcRepository.update(npc.copy(imagePath = path))
        // The old portrait is now unreferenced; drop it so files don't pile up in app storage.
        if (!previous.isNullOrBlank() && previous != path) {
            ImageStore.deletePortrait(applicationContext, previous)
        }
        notifications.notifyReady(npcId, npc.fullName)
        return Result.success()
    }

    private suspend fun renderRemote(npcId: Long, name: String, request: PortraitRequest): String {
        val submitted = queueClient.submit(request)
        var elapsed = 0L
        var consecutiveErrors = 0
        while (elapsed < MAX_WAIT_MS) {
            val status = try {
                queueClient.status(submitted.jobId)
            } catch (e: IOException) {
                // One flaky poll shouldn't kill a multi-minute render; give up only after several in a row.
                if (++consecutiveErrors >= MAX_POLL_ERRORS) throw e
                delay(POLL_INTERVAL_MS)
                elapsed += POLL_INTERVAL_MS
                continue
            }
            consecutiveErrors = 0
            when (status.state) {
                "done" -> {
                    val bytes = queueClient.decode(status.image ?: error("no image in response"))
                    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: error("could not decode image")
                    val stored = ImageStore.persistBitmap(applicationContext, bmp) ?: error("could not persist image")
                    bmp.recycle()
                    return stored
                }
                "error" -> error(status.error ?: "render error")
                "processing" -> notifications.notifyProgress(npcId, name, str(R.string.portrait_notification_generating))
                else -> notifications.notifyProgress(npcId, name, queueText(status.ahead))
            }
            delay(POLL_INTERVAL_MS)
            elapsed += POLL_INTERVAL_MS
        }
        error("render timed out")
    }

    private fun queueText(ahead: Int): String =
        if (ahead <= 0) {
            str(R.string.portrait_notification_generating)
        } else {
            applicationContext.getString(R.string.portrait_notification_in_queue, ahead)
        }

    private fun str(id: Int) = applicationContext.getString(id)

    companion object {
        const val KEY_NPC_ID = "npc_id"
        private const val POLL_INTERVAL_MS = 2_000L

        // Stay under WorkManager's 10-minute execution cap so we surface a clean "failed"
        // notification instead of being force-stopped mid-poll at the boundary.
        private const val MAX_WAIT_MS = 9 * 60_000L
        private const val MAX_POLL_ERRORS = 5
        private const val FOREGROUND_FALLBACK_ID = 0

        /** One background job per NPC; a second tap while one is running is ignored. */
        fun enqueue(context: Context, npcId: Long) {
            val request = OneTimeWorkRequestBuilder<GeneratePortraitWorker>()
                .setInputData(workDataOf(KEY_NPC_ID to npcId))
                // Start promptly and survive the app going to background; falls back to normal
                // work (no crash) if the expedited quota is exhausted.
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "portrait_$npcId",
                androidx.work.ExistingWorkPolicy.KEEP,
                request
            )
        }
    }
}
