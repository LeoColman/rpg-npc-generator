package me.kerooker.rpgnpcgenerator.repository.image

import android.util.Log

/**
 * Thin JNI surface over stable-diffusion.cpp (see app/src/main/cpp/sd_jni.cpp). The native library
 * is only present for the abiFilters we build (x86_64, arm64-v8a); [available] reports whether it
 * loaded so callers can fall back to a stub on unsupported ABIs or in host-side unit tests.
 */
object SdCppNative {

    val available: Boolean = runCatching { System.loadLibrary("sdjni") }
        .onFailure { Log.w("SdCppNative", "libsdjni not loaded: ${it.message}") }
        .isSuccess

    /** Loads a model and returns an opaque context pointer, or 0 on failure. */
    external fun nativeInit(modelPath: String, vaePath: String?, taesdPath: String?, threads: Int): Long

    /** Runs txt2img on [ctxPtr]; returns ARGB_8888 pixels (width*height) or null on failure. */
    external fun nativeGenerate(
        ctxPtr: Long,
        prompt: String,
        negativePrompt: String?,
        width: Int,
        height: Int,
        steps: Int,
        cfg: Float,
        seed: Long
    ): IntArray?

    external fun nativeFree(ctxPtr: Long)
}
