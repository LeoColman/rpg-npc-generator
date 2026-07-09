package me.kerooker.rpgnpcgenerator.repository.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Renders on the server via FastSD CPU's `POST /api/generate` and decodes the returned base64 PNG.
 * Uses the commercial-safe SD 1.5 + LCM-LoRA path (~20s/image on the Hetzner i7) rather than the
 * faster-but-non-commercial OpenVINO/SDXS path. Falls back to on-device generation ([fallback]) when
 * the server is unreachable, so portraits still work offline.
 */
class RemotePortraitGenerator(
    private val config: RemoteImageConfig,
    private val fallback: PortraitGenerator
) : PortraitGenerator {

    // encodeDefaults=true is REQUIRED: the FastSD-config fields below are all defaults, and without
    // this kotlinx.serialization omits them, so the server silently uses its own (non-OpenVINO) defaults.
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun isReady(): Boolean = true

    override suspend fun generate(request: PortraitRequest): Bitmap {
        if (config.enabled) {
            runCatching { renderRemote(request) }
                .onSuccess { return it }
                .onFailure { Log.w(TAG, "remote render failed, falling back to on-device", it) }
        }
        return fallback.generate(request)
    }

    private suspend fun renderRemote(request: PortraitRequest): Bitmap = withContext(Dispatchers.IO) {
        val body = json.encodeToString(
            FastSdRequest.serializer(),
            FastSdRequest(
                prompt = request.prompt,
                negative_prompt = request.negativePrompt,
                image_width = request.width,
                image_height = request.height
            )
        )
        val connection = (URL("${config.baseUrl.trimEnd('/')}/api/generate").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = CONNECT_TIMEOUT_MS
            readTimeout = READ_TIMEOUT_MS
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", basicAuthHeader())
        }
        try {
            connection.outputStream.use { it.write(body.toByteArray()) }
            val code = connection.responseCode
            if (code != HttpURLConnection.HTTP_OK) {
                throw IOException("generate HTTP $code: ${connection.errorStream?.readBytes()?.decodeToString()?.take(ERROR_SNIPPET)}")
            }
            val payload = connection.inputStream.use { it.readBytes().decodeToString() }
            val image = json.decodeFromString(FastSdResponse.serializer(), payload).images.firstOrNull()
                ?: throw IOException("generate response had no images")
            decodeBase64(image)
        } finally {
            connection.disconnect()
        }
    }

    private fun decodeBase64(image: String): Bitmap {
        // Strip a data URI prefix (data:image/png;base64,....) if present.
        val raw = image.substringAfter(',', image)
        val bytes = Base64.decode(raw, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            ?: throw IOException("could not decode returned image")
    }

    private fun basicAuthHeader(): String {
        val token = Base64.encodeToString("${config.username}:${config.password}".toByteArray(), Base64.NO_WRAP)
        return "Basic $token"
    }

    // FastSD CPU LCMDiffusionSetting. Commercial-safe combo: SD 1.5 base (CreativeML OpenRAIL-M) +
    // LCM-LoRA (openrail++) + tiny autoencoder, 4 steps. The app fixes these and only varies
    // prompt/size. (The OpenVINO/SDXS path is ~10x faster but its models are non-commercial.)
    @Serializable
    private data class FastSdRequest(
        val prompt: String,
        val negative_prompt: String,
        val image_width: Int,
        val image_height: Int,
        val use_openvino: Boolean = false,
        val use_lcm_lora: Boolean = true,
        val lcm_lora: LcmLora = LcmLora(),
        val use_tiny_auto_encoder: Boolean = true,
        val inference_steps: Int = 4,
        val guidance_scale: Float = 1.0f,
        val diffusion_task: String = "text_to_image",
        val number_of_images: Int = 1,
        val use_seed: Boolean = false
    )

    @Serializable
    private data class LcmLora(
        val base_model_id: String = "stable-diffusion-v1-5/stable-diffusion-v1-5",
        val lcm_lora_id: String = "latent-consistency/lcm-lora-sdv1-5"
    )

    @Serializable
    private data class FastSdResponse(val images: List<String> = emptyList())

    private companion object {
        const val TAG = "RemotePortraitGen"
        const val CONNECT_TIMEOUT_MS = 15_000
        const val READ_TIMEOUT_MS = 180_000
        const val ERROR_SNIPPET = 300
    }
}
