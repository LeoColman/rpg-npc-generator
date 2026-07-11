package me.kerooker.rpgnpcgenerator.data

import android.content.Context
import kotlinx.serialization.json.Json
import me.kerooker.rpgnpcgenerator.ui.util.ImageStore
import java.io.File
import java.util.Base64

/**
 * Serialises the saved roster to a single self-contained JSON backup and restores it again. Portraits
 * are read from their local files and embedded as base64 on export, then written back into fresh
 * portrait files on import — so a backup made on one device restores completely on another.
 *
 * Stateless: file/DB access is passed in per call (portrait paths are absolute, imports get a [Context]
 * for the portraits directory), which keeps it easy to unit-test.
 */
object RosterBackupService {

    const val CURRENT_VERSION = 1

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
        // Tolerate newer backups that add fields this build doesn't know about.
        ignoreUnknownKeys = true
    }

    /** Encodes [npcs] (with their portrait bytes inlined as base64) into the backup JSON string. */
    fun exportJson(npcs: List<Npc>): String {
        val backups = npcs.map { npc -> npc.toBackup(encodePortrait(npc.imagePath)) }
        return json.encodeToString(RosterBackup(CURRENT_VERSION, backups))
    }

    /**
     * Decodes [jsonText] and materialises each entry into an insertable [Npc], writing any embedded
     * portrait into a new file under the app's portraits directory. Returned NPCs have id 0 so the
     * caller's [NpcRepository.insert] assigns fresh ids (import is additive — it never touches existing
     * rows). Throws if [jsonText] is not a valid backup.
     */
    fun importNpcs(context: Context, jsonText: String): List<Npc> {
        val backup = json.decodeFromString<RosterBackup>(jsonText)
        return backup.npcs.map { entry ->
            val imagePath = entry.portraitJpegBase64?.let { writePortrait(context, it) }
            entry.toNpc(imagePath)
        }
    }

    /** Reads the portrait file at [path] and base64-encodes it; null if there's no path or it can't be read. */
    private fun encodePortrait(path: String?): String? {
        if (path.isNullOrBlank()) return null
        return runCatching { Base64.getEncoder().encodeToString(File(path).readBytes()) }.getOrNull()
    }

    /** Decodes a base64 JPEG into a fresh portrait file, returning its path (null if the data is unusable). */
    private fun writePortrait(context: Context, base64: String): String? {
        val bytes = runCatching { Base64.getDecoder().decode(base64) }.getOrNull() ?: return null
        return ImageStore.persistJpegBytes(context, bytes)
    }
}
