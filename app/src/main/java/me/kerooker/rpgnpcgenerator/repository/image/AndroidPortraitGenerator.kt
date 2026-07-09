package me.kerooker.rpgnpcgenerator.repository.image

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.math.abs

/**
 * Renders via stable-diffusion.cpp when the native lib and a model are available, otherwise falls
 * back to a deterministic placeholder so the feature is exercisable on any device/emulator without a
 * model present. A [Mutex] serializes generations: the CPU backend is heavy and one context is
 * shared across calls.
 */
class AndroidPortraitGenerator(
    private val modelManager: SdModelManager,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default
) : PortraitGenerator {

    private val mutex = Mutex()
    private var ctxPtr: Long = 0L
    private var loadedModelPath: String? = null

    override suspend fun isReady(): Boolean = SdCppNative.available && modelManager.isModelPresent()

    override suspend fun generate(request: PortraitRequest): Bitmap = withContext(dispatcher) {
        mutex.withLock {
            if (isReady()) runCatching { generateNative(request) }.getOrElse { error ->
                Log.e(TAG, "native generation failed, using placeholder", error)
                placeholder(request)
            } else {
                placeholder(request)
            }
        }
    }

    private fun generateNative(request: PortraitRequest): Bitmap {
        val model = modelManager.modelFile() ?: error("model missing")
        if (ctxPtr == 0L || loadedModelPath != model.absolutePath) {
            freeContext()
            ctxPtr = SdCppNative.nativeInit(
                modelPath = model.absolutePath,
                vaePath = modelManager.vaeFile()?.absolutePath,
                taesdPath = modelManager.taesdFile()?.absolutePath,
                threads = threadCount()
            )
            check(ctxPtr != 0L) { "new_sd_ctx failed for ${model.absolutePath}" }
            loadedModelPath = model.absolutePath
        }
        val pixels = SdCppNative.nativeGenerate(
            ctxPtr = ctxPtr,
            prompt = request.prompt,
            negativePrompt = request.negativePrompt,
            width = request.width,
            height = request.height,
            steps = request.steps,
            cfg = request.cfgScale,
            seed = request.seed
        ) ?: error("generate_image returned null")
        return Bitmap.createBitmap(pixels, request.width, request.height, Bitmap.Config.ARGB_8888)
    }

    private fun freeContext() {
        if (ctxPtr != 0L) SdCppNative.nativeFree(ctxPtr)
        ctxPtr = 0L
        loadedModelPath = null
    }

    /** Deterministic gradient keyed on the prompt so distinct NPCs get distinct placeholders. */
    private fun placeholder(request: PortraitRequest): Bitmap {
        val bitmap = Bitmap.createBitmap(request.width, request.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val hue = abs(request.prompt.hashCode()) % HUE_RANGE
        val top = Color.HSVToColor(floatArrayOf(hue.toFloat(), SATURATION, VALUE_TOP))
        val bottom = Color.HSVToColor(floatArrayOf((hue + HUE_SHIFT) % HUE_RANGE.toFloat(), SATURATION, VALUE_BOTTOM))
        val paint = Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, request.height.toFloat(), top, bottom, Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, request.width.toFloat(), request.height.toFloat(), paint)
        return bitmap
    }

    private fun threadCount(): Int = Runtime.getRuntime().availableProcessors().coerceIn(1, MAX_THREADS)

    private companion object {
        const val TAG = "PortraitGenerator"
        const val MAX_THREADS = 4
        const val HUE_RANGE = 360
        const val HUE_SHIFT = 40
        const val SATURATION = 0.45f
        const val VALUE_TOP = 0.55f
        const val VALUE_BOTTOM = 0.30f
    }
}
