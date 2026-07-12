package me.kerooker.rpgnpcgenerator.ui.share

import me.kerooker.rpgnpcgenerator.data.Npc
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.abilityModifier
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.formatAbilityModifier

/**
 * The file format an NPC sheet can be exported as. Each carries the MIME type used for its cache file
 * and the `ACTION_SEND` intent, so callers never hard-code the string in two places.
 */
enum class NpcExportFormat(val mimeType: String) {
    PNG("image/png"),
    PDF("application/pdf")
}

/**
 * A pending "export this NPC as [format]" request. The off-screen capture host renders the sheet once
 * and then delivers it in the requested format; a null request means nothing is being exported.
 */
data class NpcExportRequest(val npc: Npc, val format: NpcExportFormat)

/**
 * True when the NPC carries any combat stat worth rendering on the export sheet. NPCs saved before the
 * combat block existed carry none, so the whole section is skipped for them.
 */
fun Npc.hasCombatStats(): Boolean =
    listOf(strength, dexterity, constitution, intelligence, wisdom, charisma, armorClass, hitPoints)
        .any { it != null } || !challengeRating.isNullOrBlank()

/**
 * Formats an ability score with its D&D 5e modifier the way a stat block does, e.g. `14` -> "14 (+2)"
 * and `8` -> "8 (-1)". Pure and resource-free so it can be unit-tested off-device. A null score (the
 * ability was never set) returns null so callers can drop it rather than print an empty cell.
 */
fun formatAbilityScore(score: Long?): String? =
    score?.let { "$it (${formatAbilityModifier(abilityModifier(it.toInt()))})" }
