package me.kerooker.rpgnpcgenerator.repository.image

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.kerooker.rpgnpcgenerator.data.Npc
import me.kerooker.rpgnpcgenerator.ui.util.ImageStore

/** Generates a portrait for an NPC and persists it, returning the stored file path. */
class PortraitRepository(
    private val context: Context,
    private val generator: PortraitGenerator
) {

    suspend fun isReady(): Boolean = generator.isReady()

    suspend fun generateFor(npc: Npc): String {
        val bitmap = generator.generate(PortraitPrompt.forNpc(npc))
        val path = withContext(Dispatchers.IO) { ImageStore.persistBitmap(context, bitmap) }
        bitmap.recycle()
        return path ?: error("Could not persist generated portrait")
    }
}
