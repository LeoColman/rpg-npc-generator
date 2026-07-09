package me.kerooker.rpgnpcgenerator.repository.image

import android.content.Context
import android.graphics.BitmapFactory
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
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

/**
 * Generates an NPC portrait in the background so the user can leave and come back. Submits to the
 * server queue, polls for position/result (surfacing progress in a notification), then writes the
 * image to the NPC in the DB and posts a "ready" notification. Falls back to on-device rendering if
 * the server is unreachable. The portrait appears automatically when the DB row updates.
 */
class GeneratePortraitWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params), KoinComponent {

    private val npcRepository: NpcRepository by inject()
    private val queueClient: PortraitQueueClient by inject()
    private val fallback: PortraitGenerator by inject()
    private val notifications: PortraitNotifications by inject()

    override suspend fun doWork(): Result {
        val npcId = inputData.getLong(KEY_NPC_ID, -1L)
        if (npcId < 0) return Result.failure()
        val npc = npcRepository.get(npcId).firstOrNull() ?: return Result.failure()
        val request = PortraitPrompt.forNpc(npc)

        notifications.ensureChannel()
        notifications.notifyProgress(npcId, npc.fullName, str(R.string.portrait_notification_queued))

        val path = runCatching { renderRemote(npcId, npc.fullName, request) }
            .getOrElse { runCatching { renderOnDevice(request) }.getOrNull() }

        if (path == null) {
            notifications.notifyFailed(npcId, npc.fullName)
            return Result.failure()
        }
        npcRepository.update(npc.copy(imagePath = path))
        notifications.notifyReady(npcId, npc.fullName)
        return Result.success()
    }

    private suspend fun renderRemote(npcId: Long, name: String, request: PortraitRequest): String {
        val submitted = queueClient.submit(request)
        var elapsed = 0L
        while (elapsed < MAX_WAIT_MS) {
            val status = queueClient.status(submitted.jobId)
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

    private suspend fun renderOnDevice(request: PortraitRequest): String {
        val bmp = fallback.generate(request)
        val stored = ImageStore.persistBitmap(applicationContext, bmp) ?: error("could not persist image")
        bmp.recycle()
        return stored
    }

    private fun queueText(ahead: Int): String =
        if (ahead <= 0) str(R.string.portrait_notification_generating)
        else applicationContext.getString(R.string.portrait_notification_in_queue, ahead)

    private fun str(id: Int) = applicationContext.getString(id)

    companion object {
        const val KEY_NPC_ID = "npc_id"
        private const val POLL_INTERVAL_MS = 2_000L
        private const val MAX_WAIT_MS = 10 * 60_000L

        /** One background job per NPC; a second tap while one is running is ignored. */
        fun enqueue(context: Context, npcId: Long) {
            val request = OneTimeWorkRequestBuilder<GeneratePortraitWorker>()
                .setInputData(workDataOf(KEY_NPC_ID to npcId))
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "portrait_$npcId",
                androidx.work.ExistingWorkPolicy.KEEP,
                request
            )
        }
    }
}
