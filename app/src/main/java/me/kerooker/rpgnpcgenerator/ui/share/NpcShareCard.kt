package me.kerooker.rpgnpcgenerator.ui.share

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import me.kerooker.rpgnpcgenerator.data.Npc

private val CARD_WIDTH = 360.dp
private val PORTRAIT_WIDTH = 168.dp
private const val PORTRAIT_ASPECT = 512f / 640f

/**
 * A self-contained, branded sheet that renders an NPC's full profile for export/sharing: portrait,
 * identity, profession/alignment/motivation, personality traits, languages, items and — when present —
 * the D&D 5e combat stat block. Pulls every colour from the active Material theme so it matches the app,
 * and takes a pre-decoded [portrait] bitmap (the capture host loads it synchronously) rather than
 * loading async — a capture must be complete the instant it is snapshotted. Safe to render off-screen:
 * it depends on nothing but its arguments.
 */
@Composable
fun NpcShareCard(
    npc: Npc,
    portrait: ImageBitmap?,
    footer: String,
    labels: NpcShareCardLabels,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.colorScheme
    Column(
        modifier = modifier
            .width(CARD_WIDTH)
            .clip(RoundedCornerShape(28.dp))
            .background(colors.surface)
    ) {
        Header(npc, portrait, colors)
        Body(npc, labels, colors)
        Footer(footer, colors)
    }
}

/** Localized labels for the sheet's sections, gathered by the caller via `stringResource`. */
data class NpcShareCardLabels(
    val profession: String,
    val alignment: String,
    val motivation: String,
    val personality: String,
    val languages: String,
    val items: String,
    val combat: CombatSheetLabels
)

/** Localized labels for the combat stat block, kept apart so the sheet stays resource-free. */
data class CombatSheetLabels(
    val title: String,
    val strength: String,
    val dexterity: String,
    val constitution: String,
    val intelligence: String,
    val wisdom: String,
    val charisma: String,
    val armorClass: String,
    val hitPoints: String,
    val challengeRating: String
)

@Composable
private fun Header(npc: Npc, portrait: ImageBitmap?, colors: ColorScheme) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.primaryContainer)
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        PortraitFrame(portrait, colors)
        Text(
            text = npc.fullName.ifBlank { " " },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = colors.onPrimaryContainer,
            textAlign = TextAlign.Center
        )
        if (npc.nickname.isNotBlank()) {
            Text(
                text = "“${npc.nickname}”",
                style = MaterialTheme.typography.titleMedium,
                fontStyle = FontStyle.Italic,
                color = colors.onPrimaryContainer.copy(alpha = 0.85f),
                textAlign = TextAlign.Center
            )
        }
        val meta = listOf(npc.race, npc.age, npc.gender)
            .filter { it.isNotBlank() }
            .joinToString(" · ")
        if (meta.isNotBlank()) {
            Text(
                text = meta,
                style = MaterialTheme.typography.labelLarge,
                color = colors.onPrimaryContainer.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PortraitFrame(portrait: ImageBitmap?, colors: ColorScheme) {
    Box(
        modifier = Modifier
            .width(PORTRAIT_WIDTH)
            .aspectRatio(PORTRAIT_ASPECT)
            .clip(RoundedCornerShape(18.dp))
            .background(colors.secondaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (portrait != null) {
            Image(
                bitmap = portrait,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = colors.onSecondaryContainer,
                modifier = Modifier.size(72.dp)
            )
        }
    }
}

@Composable
private fun Body(npc: Npc, labels: NpcShareCardLabels, colors: ColorScheme) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Attribute(labels.profession, npc.profession, colors)
        Attribute(labels.alignment, npc.alignment, colors)
        Attribute(labels.motivation, npc.motivation, colors)
        ChipSection(labels.personality, npc.personalityTraits, colors)
        ChipSection(labels.languages, npc.languages, colors)
        ChipSection(labels.items, npc.items, colors)
        CombatBlock(npc, labels.combat, colors)
    }
}

@Composable
private fun Attribute(label: String, value: String, colors: ColorScheme) {
    if (value.isBlank()) return
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        SectionLabel(label, colors)
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface
        )
    }
}

/** A titled row of rounded chips (personality traits, languages). Hidden when nothing is filled in. */
@Composable
private fun ChipSection(label: String, items: List<String>, colors: ColorScheme) {
    val visible = items.filter { it.isNotBlank() }
    if (visible.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(label, colors)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            visible.forEach { item ->
                Text(
                    text = item,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.onTertiaryContainer,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(colors.tertiaryContainer)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

/**
 * The D&D 5e combat block: ability scores (each with its modifier) laid out two-per-row, then Armor
 * Class, Hit Points and Challenge Rating as chips. Entirely skipped for NPCs that carry no stats.
 */
@Composable
private fun CombatBlock(npc: Npc, labels: CombatSheetLabels, colors: ColorScheme) {
    if (!npc.hasCombatStats()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel(labels.title, colors)
        abilityEntries(npc, labels).chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { (label, value) -> StatCell(label, value, colors) }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        val derived = derivedEntries(npc, labels)
        if (derived.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                derived.forEach { (label, value) -> DerivedChip(label, value, colors) }
            }
        }
    }
}

/** The filled-in ability scores as label/"score (modifier)" pairs, in stat-block order. */
private fun abilityEntries(npc: Npc, labels: CombatSheetLabels): List<Pair<String, String>> =
    listOf(
        labels.strength to npc.strength,
        labels.dexterity to npc.dexterity,
        labels.constitution to npc.constitution,
        labels.intelligence to npc.intelligence,
        labels.wisdom to npc.wisdom,
        labels.charisma to npc.charisma
    ).mapNotNull { (label, score) -> formatAbilityScore(score)?.let { label to it } }

/** The filled-in derived stats (Armor Class, Hit Points, Challenge Rating) as label/value pairs. */
private fun derivedEntries(npc: Npc, labels: CombatSheetLabels): List<Pair<String, String>> =
    listOfNotNull(
        npc.armorClass?.let { labels.armorClass to it.toString() },
        npc.hitPoints?.let { labels.hitPoints to it.toString() },
        npc.challengeRating?.takeIf { it.isNotBlank() }?.let { labels.challengeRating to it }
    )

/** One ability score cell, e.g. "STR" over "14 (+2)", taking an equal share of its row. */
@Composable
private fun RowScope.StatCell(label: String, value: String, colors: ColorScheme) {
    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = colors.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.onSurface
        )
    }
}

/** A "label value" pill for the derived stats (Armor Class, Hit Points, Challenge Rating). */
@Composable
private fun DerivedChip(label: String, value: String, colors: ColorScheme) {
    Text(
        text = "$label $value",
        style = MaterialTheme.typography.labelLarge,
        color = colors.onSecondaryContainer,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(colors.secondaryContainer)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    )
}

@Composable
private fun SectionLabel(label: String, colors: ColorScheme) {
    Text(
        text = label.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = colors.primary
    )
}

@Composable
private fun Footer(footer: String, colors: ColorScheme) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surfaceVariant)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.AutoAwesome,
            contentDescription = null,
            tint = colors.primary,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = footer,
            style = MaterialTheme.typography.labelMedium,
            color = colors.onSurfaceVariant
        )
    }
}
