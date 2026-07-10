package me.kerooker.portrait.queue

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO as ClientCIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.UUID

/**
 * Single-worker FIFO queue in front of FastSD, in Kotlin/Ktor (the app + backend share one stack).
 *
 * FastSD renders one image at a time, so concurrent users would otherwise block invisibly. This
 * proxy accepts submissions immediately, assigns a queue position, and drains them serially through
 * FastSD, so the app can show "you're #N in line" / "generating now" and poll for the result. The
 * submit body is forwarded to FastSD verbatim, so the app chooses the model per request.
 */

private val FASTSD_URL = System.getenv("FASTSD_URL") ?: "http://fastsd:8000/api/generate"
private val JOB_TTL_SECONDS = (System.getenv("JOB_TTL_SECONDS") ?: "900").toLong()
private val RENDER_TIMEOUT_MS = ((System.getenv("RENDER_TIMEOUT") ?: "600").toDouble() * 1000).toLong()
private val PORT = (System.getenv("PORT") ?: "9000").toInt()

@Serializable
data class SubmitResponse(@SerialName("job_id") val jobId: String, val ahead: Int)

@Serializable
data class StatusResponse(
    val state: String,                              // queued | processing | done | error
    val ahead: Int,                                 // how many render before you
    @SerialName("queue_length") val queueLength: Int,
    val image: String?,                             // base64 PNG when done
    val error: String?,
)

@Serializable
data class RootResponse(val ok: Boolean, val waiting: Int, val processing: Boolean)

@Serializable
data class QueueSizeResponse(val size: Int, val waiting: Int, val processing: Boolean)

@Serializable
data class CancelResponse(val cancelled: Boolean)

@Serializable
data class UnknownResponse(val state: String = "unknown")

private val serverJson = Json { encodeDefaults = true }
private val parseJson = Json { ignoreUnknownKeys = true; isLenient = true }

/**
 * Holds all queue state and the render step. Kept as an injectable object (rather than globals) so
 * the HTTP client (→ FastSD) can be mocked and the clock controlled in tests. Thread-safe via a
 * [Mutex]; the single worker releases the lock across the long FastSD call.
 */
class QueueService(
    private val client: HttpClient,
    private val fastsdUrl: String,
    private val jobTtlSeconds: Long = 900,
    private val clock: () -> Long = { System.currentTimeMillis() / 1000 },
) {
    private class Job(
        var state: String,
        val body: String,
        var image: String? = null,
        var error: String? = null,
        var updated: Long,
    )

    private val jobs = LinkedHashMap<String, Job>()   // job_id -> job
    private val waiting = ArrayDeque<String>()          // job ids still queued, FIFO order
    private var current: String? = null                 // job id currently rendering
    private val lock = Mutex()
    private val wakeup = Channel<Unit>(Channel.CONFLATED)

    suspend fun submit(rawBody: String): SubmitResponse = lock.withLock {
        pruneLocked()
        val jid = UUID.randomUUID().toString().replace("-", "")
        jobs[jid] = Job(state = "queued", body = rawBody, updated = clock())
        waiting.addLast(jid)
        wakeup.trySend(Unit)
        // ahead = jobs that must finish before this one starts.
        SubmitResponse(jid, (waiting.size - 1) + if (current != null) 1 else 0)
    }

    /** Snapshot of a job, or null if it is unknown/expired. */
    suspend fun status(jid: String): StatusResponse? = lock.withLock {
        pruneLocked()
        val job = jobs[jid] ?: return@withLock null
        val ahead = if (job.state == "queued") {
            waiting.indexOf(jid).coerceAtLeast(0) + if (current != null) 1 else 0
        } else 0
        StatusResponse(
            state = job.state,
            ahead = ahead,
            queueLength = queueLengthLocked(),
            image = job.image,
            error = job.error,
        )
    }

    suspend fun snapshot(): RootResponse = lock.withLock {
        RootResponse(ok = true, waiting = waiting.size, processing = current != null)
    }

    /** Total jobs in front of a new submission: those waiting plus the one rendering, if any. */
    suspend fun size(): QueueSizeResponse = lock.withLock {
        pruneLocked()
        QueueSizeResponse(
            size = queueLengthLocked(),
            waiting = waiting.size,
            processing = current != null,
        )
    }

    /**
     * Drops a job so it stops occupying the line. A still-[waiting] job is removed before it ever
     * renders; a finished job is just forgotten. A job already rendering ([current]) can't be
     * interrupted mid-FastSD-call, so it is left to finish and the client discards the result.
     * Returns true if the job existed and was removed, false if unknown or currently rendering.
     */
    suspend fun cancel(jid: String): Boolean = lock.withLock {
        pruneLocked()
        if (jid == current || jobs.remove(jid) == null) return@withLock false
        waiting.remove(jid)
        true
    }

    /** Drains the FIFO forever: forward each body to FastSD one at a time, store the image or error. */
    suspend fun runWorker() {
        while (true) {
            if (!processNext()) wakeup.receive()
        }
    }

    /**
     * Renders exactly one queued job (forward to FastSD, store result). Returns false if the queue
     * was empty. Split out from [runWorker] so tests can drive the queue one deterministic step at a
     * time without racing the loop.
     */
    internal suspend fun processNext(): Boolean {
        val jid = lock.withLock {
            pruneLocked()
            if (waiting.isEmpty()) {
                null
            } else {
                val next = waiting.removeFirst()
                current = next
                jobs[next]?.apply { state = "processing"; updated = clock() }
                next
            }
        } ?: return false
        val job = lock.withLock { jobs[jid] }
        if (job == null) {
            lock.withLock { current = null }
            return true
        }
        try {
            val response = client.post(fastsdUrl) {
                contentType(ContentType.Application.Json)
                setBody(job.body)
            }
            val data = parseJson.parseToJsonElement(response.bodyAsText()).jsonObject
            val images = data["images"]?.jsonArray
            lock.withLock {
                if (!images.isNullOrEmpty()) {
                    job.image = images[0].jsonPrimitive.content
                    job.state = "done"
                } else {
                    job.error = data["error"]?.jsonPrimitive?.content
                        ?: "no image (HTTP ${response.status.value})"
                    job.state = "error"
                }
            }
        } catch (exc: Exception) {
            lock.withLock {
                job.error = exc.message ?: exc.toString()
                job.state = "error"
            }
        } finally {
            lock.withLock {
                job.updated = clock()
                current = null
            }
        }
        return true
    }

    /** Jobs occupying the line: those queued plus the one rendering, if any. Caller holds [lock]. */
    private fun queueLengthLocked(): Int = waiting.size + if (current != null) 1 else 0

    /** Drop finished jobs whose result has outlived the TTL. Caller must hold [lock]. */
    private fun pruneLocked() {
        val now = clock()
        jobs.entries.removeIf { (_, v) -> v.state in FINISHED && now - v.updated > jobTtlSeconds }
    }

    private companion object {
        val FINISHED = setOf("done", "error")
    }
}

fun main() {
    val client = HttpClient(ClientCIO) {
        install(HttpTimeout) {
            requestTimeoutMillis = RENDER_TIMEOUT_MS
            socketTimeoutMillis = RENDER_TIMEOUT_MS
        }
    }
    val service = QueueService(client, FASTSD_URL, JOB_TTL_SECONDS)
    embeddedServer(CIO, port = PORT, host = "0.0.0.0") { queueModule(service) }.start(wait = true)
}

/** Wires the HTTP surface to [service]. [startWorker] is false in route tests so state stays put. */
fun Application.queueModule(service: QueueService, startWorker: Boolean = true) {
    install(ContentNegotiation) { json(serverJson) }

    if (startWorker) launch { service.runWorker() }

    routing {
        post("/submit") {
            val raw = call.receiveText()
            // Validate it parses as JSON (matches FastAPI's request.json() 400 on garbage).
            runCatching { parseJson.parseToJsonElement(raw) }.onFailure {
                call.respond(HttpStatusCode.BadRequest, UnknownResponse(state = "invalid_json"))
                return@post
            }
            call.respond(service.submit(raw))
        }

        get("/status/{jid}") {
            val response = service.status(call.parameters["jid"].orEmpty())
            if (response == null) {
                call.respond(HttpStatusCode.NotFound, UnknownResponse())
            } else {
                call.respond(response)
            }
        }

        // Lets the app interrupt a submission (e.g. the user re-rolled the NPC) so it frees the line.
        delete("/jobs/{jid}") {
            val cancelled = service.cancel(call.parameters["jid"].orEmpty())
            val status = if (cancelled) HttpStatusCode.OK else HttpStatusCode.NotFound
            call.respond(status, CancelResponse(cancelled))
        }

        get("/") {
            call.respond(service.snapshot())
        }

        // Public queue size (carved out of basic_auth in caddy) so clients can show line length.
        get("/queue") {
            call.respond(service.size())
        }
    }
}
