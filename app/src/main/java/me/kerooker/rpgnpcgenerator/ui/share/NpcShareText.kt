package me.kerooker.rpgnpcgenerator.ui.share

import me.kerooker.rpgnpcgenerator.data.Npc

/**
 * Builds the plain-text body that rides along with the shared image (used verbatim by chat apps that
 * ignore the attachment, and as the `EXTRA_TEXT` caption everywhere else).
 *
 * Pure and resource-free so it can be unit-tested off-device: every user-facing word comes from the
 * NPC itself except [footer], which the caller passes already localized (e.g. "Created with <app>").
 * Blank attributes are skipped so half-filled NPCs still read cleanly.
 */
fun npcShareText(npc: Npc, footer: String): String {
    val lines = buildList {
        add(npc.fullName.trim())
        npc.nickname.trimNonBlank()?.let { add("“$it”") }
        listOf(npc.race, npc.age, npc.gender)
            .mapNotNull { it.trimNonBlank() }
            .takeIf { it.isNotEmpty() }
            ?.let { add(it.joinToString(" · ")) }
        npc.profession.trimNonBlank()?.let(::add)
        npc.alignment.trimNonBlank()?.let(::add)
        npc.motivation.trimNonBlank()?.let(::add)
        npc.personalityTraits
            .mapNotNull { it.trimNonBlank() }
            .takeIf { it.isNotEmpty() }
            ?.let { add(it.joinToString(" · ")) }
    }.filter { it.isNotBlank() }

    return (lines + listOf("", footer)).joinToString("\n")
}

private fun String.trimNonBlank(): String? = trim().takeIf { it.isNotEmpty() }
