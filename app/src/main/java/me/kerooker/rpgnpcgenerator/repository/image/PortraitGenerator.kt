package me.kerooker.rpgnpcgenerator.repository.image

import android.graphics.Bitmap

/**
 * Parameters for a single txt2img run. Defaults are coherent SD 1.5 settings (512px, 20 steps,
 * CFG 7). These render well on the server; on-device (offline fallback) they're correct but slow.
 */
data class PortraitRequest(
    val prompt: String,
    val negativePrompt: String,
    val width: Int = 512,
    val height: Int = 640,
    val steps: Int = 20,
    val cfgScale: Float = 7f,
    val seed: Long = -1L
)

/**
 * Engine-agnostic portrait generator. Implementations may render on-device (stable-diffusion.cpp),
 * remotely, or produce a placeholder. [isReady] reports whether real generation can run right now
 * (native lib present, model downloaded); when false, callers still get a usable image from a stub.
 */
interface PortraitGenerator {
    suspend fun isReady(): Boolean
    suspend fun generate(request: PortraitRequest): Bitmap
}
