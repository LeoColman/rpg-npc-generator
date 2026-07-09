package me.kerooker.rpgnpcgenerator.repository.image

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.coroutineContext

/**
 * Locates (and optionally downloads) the on-device diffusion model. The GGUF is far too large to
 * ship in the APK, so it lives in app storage. For the emulator spike you can side-load it with:
 *
 *   adb push sd-model.gguf /sdcard/Android/data/me.kerooker.rpgcharactergenerator.debug/files/models/
 *
 * Both internal filesDir/models and the external files dir are checked, internal first.
 */
class SdModelManager(private val context: Context) {

    private val internalDir: File get() = File(context.filesDir, MODELS_DIR)

    private fun resolve(name: String): File? {
        File(internalDir, name).let { if (it.exists() && it.length() > 0) return it }
        context.getExternalFilesDir(MODELS_DIR)?.let { ext ->
            File(ext, name).let { if (it.exists() && it.length() > 0) return it }
        }
        return null
    }

    // sd.cpp detects format from file content, so a raw SD 1.5 .safetensors works too.
    fun modelFile(): File? = MODEL_CANDIDATES.firstNotNullOfOrNull { resolve(it) }
    fun taesdFile(): File? = resolve(TAESD_FILE)
    fun vaeFile(): File? = resolve(VAE_FILE)

    fun isModelPresent(): Boolean = modelFile() != null

    /** Streams [url] into internal storage, reporting progress in [0f, 1f]. Returns the target file. */
    suspend fun download(url: String, onProgress: (Float) -> Unit): File = withContext(Dispatchers.IO) {
        internalDir.mkdirs()
        val target = File(internalDir, MODEL_FILE)
        val tmp = File(internalDir, "$MODEL_FILE.part")
        val connection = (URL(url).openConnection() as HttpURLConnection).apply { connectTimeout = CONNECT_TIMEOUT_MS }
        try {
            val total = connection.contentLengthLong.takeIf { it > 0 }
            connection.inputStream.use { input ->
                tmp.outputStream().use { output ->
                    val buffer = ByteArray(DOWNLOAD_BUFFER)
                    var read: Int
                    var downloaded = 0L
                    while (input.read(buffer).also { read = it } != -1) {
                        coroutineContext.ensureActive()
                        output.write(buffer, 0, read)
                        downloaded += read
                        if (total != null) onProgress(downloaded.toFloat() / total)
                    }
                }
            }
            if (!tmp.renameTo(target)) error("Could not finalize model download")
            target
        } finally {
            connection.disconnect()
            tmp.delete()
        }
    }

    private companion object {
        const val MODELS_DIR = "models"
        const val MODEL_FILE = "sd-model.gguf"
        val MODEL_CANDIDATES = listOf(MODEL_FILE, "sd-model.safetensors")
        const val TAESD_FILE = "taesd.gguf"
        const val VAE_FILE = "vae.gguf"
        const val DOWNLOAD_BUFFER = 1 shl 16
        const val CONNECT_TIMEOUT_MS = 30_000
    }
}
