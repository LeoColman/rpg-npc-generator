package me.kerooker.rpgnpcgenerator.repository.image

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/** A job accepted by the queue: its id and how many render before it. */
data class Submitted(val jobId: String, val ahead: Int)

/** Snapshot of a queued job. [state] is queued | processing | done | error | unknown. */
data class JobStatus(val state: String, val ahead: Int, val image: String?, val error: String?)

/**
 * Talks to the server-side FIFO queue in front of FastSD: `POST /submit` returns a job id + queue
 * position, `GET /status/{id}` reports progress and, when done, the base64 PNG. Lets the app submit,
 * walk away, and poll for position + result. Commercial-safe SD 1.5 + LCM-LoRA render config.
 *
 * The server config is resolved fresh per request via [configProvider] (backed by [PortraitServerStore]),
 * so a user editing the server in Settings takes effect without an app restart.
 */
class PortraitQueueClient(private val configProvider: suspend () -> RemoteImageConfig) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    /** Whether a usable server is currently configured (non-blank base URL + password). */
    suspend fun enabled(): Boolean = configProvider().enabled

    suspend fun submit(request: PortraitRequest): Submitted = withContext(Dispatchers.IO) {
        val config = configProvider()
        val body = json.encodeToString(
            SubmitBody.serializer(),
            SubmitBody(
                prompt = request.prompt,
                negativePrompt = request.negativePrompt,
                imageWidth = request.width,
                imageHeight = request.height
            )
        )
        val payload = post(config, "/submit", body)
        val res = json.decodeFromString(SubmitResponse.serializer(), payload)
        Submitted(jobId = res.jobId, ahead = res.ahead)
    }

    suspend fun status(jobId: String): JobStatus = withContext(Dispatchers.IO) {
        val config = configProvider()
        val payload = get(config, "/status/$jobId")
        val res = json.decodeFromString(StatusResponse.serializer(), payload)
        JobStatus(state = res.state, ahead = res.ahead, image = res.image, error = res.error)
    }

    fun decode(imageBase64: String): ByteArray {
        val raw = imageBase64.substringAfter(',', imageBase64)
        return Base64.decode(raw, Base64.DEFAULT)
    }

    /** Best-effort: drops a still-queued job server-side so it won't render after we've moved on. */
    suspend fun cancel(jobId: String): Unit = withContext(Dispatchers.IO) {
        val c = open(configProvider(), "/jobs/$jobId").apply { requestMethod = "DELETE" }
        try {
            c.responseCode // fire the DELETE; a 404 (already gone) is fine, so we ignore the code
        } finally {
            c.disconnect()
        }
    }

    private fun open(config: RemoteImageConfig, path: String): HttpURLConnection =
        (URL("${config.baseUrl.trimEnd('/')}$path").openConnection() as HttpURLConnection).apply {
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            setRequestProperty("Authorization", basicAuth(config))
        }

    private fun post(config: RemoteImageConfig, path: String, body: String): String {
        val c = open(config, path).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
        }
        try {
            c.outputStream.use { it.write(body.toByteArray()) }
            if (c.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("$path HTTP ${c.responseCode} ${c.errorText()}".trim())
            }
            return c.inputStream.use { it.readBytes().decodeToString() }
        } finally {
            c.disconnect()
        }
    }

    private fun get(config: RemoteImageConfig, path: String): String {
        val c = open(config, path)
        try {
            if (c.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("$path HTTP ${c.responseCode} ${c.errorText()}".trim())
            }
            return c.inputStream.use { it.readBytes().decodeToString() }
        } finally {
            c.disconnect()
        }
    }

    /** Best-effort read of the server's error body so failures carry the reason, not just a code. */
    private fun HttpURLConnection.errorText(): String =
        runCatching { errorStream?.use { it.readBytes().decodeToString() } }.getOrNull().orEmpty()

    private fun basicAuth(config: RemoteImageConfig): String {
        val token = Base64.encodeToString("${config.username}:${config.password}".toByteArray(), Base64.NO_WRAP)
        return "Basic $token"
    }

    // Commercial-safe: SD 1.5 base (OpenRAIL-M) + LCM-LoRA (openrail++), 4 steps, tiny autoencoder.
    @Serializable
    private data class SubmitBody(
        val prompt: String,
        @SerialName("negative_prompt") val negativePrompt: String,
        @SerialName("image_width") val imageWidth: Int,
        @SerialName("image_height") val imageHeight: Int,
        @SerialName("use_openvino") val useOpenvino: Boolean = false,
        @SerialName("use_lcm_lora") val useLcmLora: Boolean = true,
        @SerialName("lcm_lora") val lcmLora: LcmLora = LcmLora(),
        @SerialName("use_tiny_auto_encoder") val useTinyAutoEncoder: Boolean = true,
        @SerialName("inference_steps") val inferenceSteps: Int = 2,
        // cfg 1.0 skips the unconditional pass, so 2 steps = 2 UNet evals — ~2.8x faster (~14s vs
        // ~40s at cfg1.5/4steps). NSFW is caught server-side by the diffusers safety_checker (which
        // blanks explicit output independently of guidance), not by the now-inert negative prompt.
        @SerialName("guidance_scale") val guidanceScale: Float = 1.0f,
        @SerialName("diffusion_task") val diffusionTask: String = "text_to_image",
        @SerialName("number_of_images") val numberOfImages: Int = 1,
        @SerialName("use_seed") val useSeed: Boolean = false,
        // Server-side NSFW filter (diffusers safety checker): blanks out unsafe renders as a last line
        // of defense beyond the prompt. Essential for an ad-supported app that also renders child NPCs.
        @SerialName("use_safety_checker") val useSafetyChecker: Boolean = true
    )

    // DreamShaper-8: fantasy-tuned SD 1.5, CreativeML OpenRAIL-M (commercial-safe), LCM-LoRA compatible.
    // Knows D&D races far better than vanilla SD 1.5; ~14s at cfg1.0/2 steps.
    @Serializable
    private data class LcmLora(
        @SerialName("base_model_id") val baseModelId: String = "Lykon/dreamshaper-8",
        @SerialName("lcm_lora_id") val lcmLoraId: String = "latent-consistency/lcm-lora-sdv1-5"
    )

    @Serializable
    private data class SubmitResponse(@SerialName("job_id") val jobId: String, val ahead: Int = 0)

    @Serializable
    private data class StatusResponse(
        val state: String,
        val ahead: Int = 0,
        val image: String? = null,
        val error: String? = null
    )

    private companion object {
        const val CONNECT_TIMEOUT_MS = 15_000
        const val READ_TIMEOUT_MS = 30_000
    }
}
