package me.kerooker.rpgnpcgenerator.viewmodel.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import me.kerooker.rpgnpcgenerator.data.NpcRepository
import me.kerooker.rpgnpcgenerator.data.RosterBackupService

/**
 * Backs the Settings screen's roster export/import. Both operations run on [Dispatchers.IO] (file and
 * database work) and are exposed as suspend functions the screen calls once it has a document [android.net.Uri]
 * from the Storage Access Framework.
 */
class SettingsViewModel(
    private val npcRepository: NpcRepository,
    private val appContext: Context
) : ViewModel() {

    /** Serialises the entire saved roster to a backup JSON string, returning it alongside the NPC count. */
    suspend fun exportJson(): ExportResult = withContext(Dispatchers.IO) {
        val npcs = npcRepository.all().first()
        ExportResult(json = RosterBackupService.exportJson(npcs), count = npcs.size)
    }

    /**
     * Restores every NPC from a backup [jsonText], inserting them as new rows, and returns how many were
     * added. Additive: existing NPCs are left untouched. Throws if [jsonText] is not a valid backup.
     */
    suspend fun import(jsonText: String): Int = withContext(Dispatchers.IO) {
        val npcs = RosterBackupService.importNpcs(appContext, jsonText)
        npcs.forEach { npcRepository.insert(it) }
        npcs.size
    }

    data class ExportResult(val json: String, val count: Int)
}
