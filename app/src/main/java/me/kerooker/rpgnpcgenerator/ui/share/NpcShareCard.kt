package me.kerooker.rpgnpcgenerator.ui.share

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
 * A self-contained, branded card that renders an NPC's key attributes for sharing. Pulls every colour
 * from the active Material theme so it matches the app, and takes a pre-decoded [portrait] bitmap (the
 * capture host loads it synchronously) rather than loading async — a share render must be complete the
 * instant it is captured. Safe to render off-screen: it depends on nothing but its arguments.
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

/** Localized labels for the card's attribute sections, gathered by the caller via `stringResource`. */
data class NpcShareCardLabels(
    val profession: String,
    val alignment: String,
    val motivation: String,
    val personality: String
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
        Traits(labels.personality, npc.personalityTraits, colors)
    }
}

@Composable
private fun Attribute(label: String, value: String, colors: ColorScheme) {
    if (value.isBlank()) return
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = colors.primary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.onSurface
        )
    }
}

@Composable
private fun Traits(label: String, traits: List<String>, colors: ColorScheme) {
    val visible = traits.filter { it.isNotBlank() }
    if (visible.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = colors.primary
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            visible.forEach { trait ->
                Text(
                    text = trait,
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
