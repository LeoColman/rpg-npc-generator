package me.kerooker.rpgnpcgenerator.repository.image

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
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
 */
class PortraitQueueClient(private val config: RemoteImageConfig) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    val enabled: Boolean get() = config.enabled

    suspend fun submit(request: PortraitRequest): Submitted = withContext(Dispatchers.IO) {
        val body = json.encodeToString(
            SubmitBody.serializer(),
            SubmitBody(
                prompt = request.prompt,
                negativePrompt = request.negativePrompt,
                imageWidth = request.width,
                imageHeight = request.height
            )
        )
        val payload = post("/submit", body)
        val res = json.decodeFromString(SubmitResponse.serializer(), payload)
        Submitted(jobId = res.jobId, ahead = res.ahead)
    }

    suspend fun status(jobId: String): JobStatus = withContext(Dispatchers.IO) {
        val payload = get("/status/$jobId")
        val res = json.decodeFromString(StatusResponse.serializer(), payload)
        JobStatus(state = res.state, ahead = res.ahead, image = res.image, error = res.error)
    }

    fun decode(imageBase64: String): ByteArray {
        val raw = imageBase64.substringAfter(',', imageBase64)
        return Base64.decode(raw, Base64.DEFAULT)
    }

    private fun open(path: String): HttpURLConnection =
        (URL("${config.baseUrl.trimEnd('/')}$path").openConnection() as HttpURLConnection).apply {
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            setRequestProperty("Authorization", basicAuth())
        }

    private fun post(path: String, body: String): String {
        val c = open(path).apply {
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

    private fun get(path: String): String {
        val c = open(path)
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

    private fun basicAuth(): String {
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
        @SerialName("inference_steps") val inferenceSteps: Int = 4,
        @SerialName("guidance_scale") val guidanceScale: Float = 1.0f,
        @SerialName("diffusion_task") val diffusionTask: String = "text_to_image",
        @SerialName("number_of_images") val numberOfImages: Int = 1,
        @SerialName("use_seed") val useSeed: Boolean = false
    )

    // DreamShaper-8: fantasy-tuned SD 1.5, CreativeML OpenRAIL-M (commercial-safe), LCM-LoRA compatible.
    // Knows D&D races far better than vanilla SD 1.5; stays ~20s because it's still SD 1.5 + LCM.
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
