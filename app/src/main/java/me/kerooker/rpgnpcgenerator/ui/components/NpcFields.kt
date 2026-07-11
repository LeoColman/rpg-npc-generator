package me.kerooker.rpgnpcgenerator.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import me.kerooker.rpgnpcgenerator.R
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.CombatStats
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.abilityModifier
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.formatAbilityModifier

/** A twenty-sided die button that spins each time it is pressed. */
@Composable
fun RerollButton(
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var spins by remember { mutableIntStateOf(0) }
    val rotation by animateFloatAsState(targetValue = spins * 360f, label = "reroll-spin")
    IconButton(
        onClick = {
            spins++
            onClick()
        },
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_twenty_sided_dice),
            contentDescription = contentDescription,
            modifier = Modifier
                .size(24.dp)
                .rotate(rotation)
        )
    }
}

/** A titled group of related NPC attributes. */
@Composable
fun FieldGroup(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        content()
    }
}

/** A single labelled NPC attribute. Shows a re-roll die when [onReroll] is provided. */
@Composable
fun NpcField(
    label: String,
    value: String,
    editable: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onReroll: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        readOnly = !editable,
        singleLine = true,
        trailingIcon = onReroll?.let { reroll ->
            { RerollButton(contentDescription = stringResource(R.string.cd_reroll, label), onClick = reroll) }
        },
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * A titled, editable list of short strings (languages, personality traits). Each row can be edited,
 * re-rolled (when [onReroll] is given) and removed while [editable]; an "add" affordance appends.
 * When [onRerollAll] is given, the header shows a die that regenerates every item at once.
 */
@Composable
fun EditableListSection(
    title: String,
    itemLabel: String,
    items: List<String>,
    editable: Boolean,
    onItemChange: (index: Int, value: String) -> Unit,
    onRemove: (index: Int) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
    onReroll: ((index: Int) -> Unit)? = null,
    onRerollAll: (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            if (editable && onRerollAll != null) {
                RerollButton(
                    contentDescription = stringResource(R.string.cd_reroll_all, title),
                    onClick = onRerollAll
                )
            }
        }
        items.forEachIndexed { index, item ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = item,
                    onValueChange = { onItemChange(index, it) },
                    label = { Text(itemLabel) },
                    readOnly = !editable,
                    singleLine = true,
                    trailingIcon = onReroll?.let { reroll ->
                        {
                            RerollButton(
                                contentDescription = stringResource(R.string.cd_reroll, itemLabel),
                                onClick = { reroll(index) }
                            )
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                if (editable) {
                    IconButton(onClick = { onRemove(index) }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.cd_remove_item, itemLabel)
                        )
                    }
                }
            }
        }
        if (editable) {
            TextButton(onClick = onAdd, modifier = Modifier.padding(top = 4.dp)) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                Text(
                    text = stringResource(R.string.random_npc_add_item),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

/** The nine combat values as editable text, so the same section serves both view and edit modes. */
data class CombatStatsUi(
    val strength: String = "",
    val dexterity: String = "",
    val constitution: String = "",
    val intelligence: String = "",
    val wisdom: String = "",
    val charisma: String = "",
    val armorClass: String = "",
    val hitPoints: String = "",
    val challengeRating: String = ""
)

/** Presents a generated [CombatStats] block as editable-text UI values. */
fun CombatStats.toUi(): CombatStatsUi = CombatStatsUi(
    strength = strength.toString(),
    dexterity = dexterity.toString(),
    constitution = constitution.toString(),
    intelligence = intelligence.toString(),
    wisdom = wisdom.toString(),
    charisma = charisma.toString(),
    armorClass = armorClass.toString(),
    hitPoints = hitPoints.toString(),
    challengeRating = challengeRating
)

/** A labelled, digits-only numeric attribute (Armor Class, Hit Points). */
@Composable
private fun NumericField(
    label: String,
    value: String,
    editable: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter(Char::isDigit)) },
        label = { Text(label) },
        readOnly = !editable,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * An ability score with its D&D 5e modifier baked into the label (e.g. "STR (+2)"). The modifier
 * recomputes live as the score is edited via floor((score - 10) / 2); a blank/invalid score shows
 * just the label.
 */
@Composable
fun AbilityScoreField(
    label: String,
    value: String,
    editable: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val score = value.toIntOrNull()
    val fullLabel = if (score != null) "$label (${formatAbilityModifier(abilityModifier(score))})" else label
    NumericField(fullLabel, value, editable, onValueChange, modifier)
}

/**
 * The "Combat stats" block: six ability scores (each showing its modifier), Armor Class, Hit Points
 * and a text Challenge Rating. Read-only when [editable] is false. Combat values are plain data and
 * never feed the portrait prompt, so editing here does not trigger a portrait re-render.
 */
@Composable
fun CombatStatsSection(
    stats: CombatStatsUi,
    editable: Boolean,
    modifier: Modifier = Modifier,
    onStatsChange: (CombatStatsUi) -> Unit = {}
) {
    FieldGroup(title = stringResource(R.string.combat_stats_label), modifier = modifier) {
        AbilityRow(
            leftLabel = stringResource(R.string.combat_strength),
            leftValue = stats.strength,
            onLeftChange = { onStatsChange(stats.copy(strength = it)) },
            rightLabel = stringResource(R.string.combat_dexterity),
            rightValue = stats.dexterity,
            onRightChange = { onStatsChange(stats.copy(dexterity = it)) },
            editable = editable
        )
        AbilityRow(
            leftLabel = stringResource(R.string.combat_constitution),
            leftValue = stats.constitution,
            onLeftChange = { onStatsChange(stats.copy(constitution = it)) },
            rightLabel = stringResource(R.string.combat_intelligence),
            rightValue = stats.intelligence,
            onRightChange = { onStatsChange(stats.copy(intelligence = it)) },
            editable = editable
        )
        AbilityRow(
            leftLabel = stringResource(R.string.combat_wisdom),
            leftValue = stats.wisdom,
            onLeftChange = { onStatsChange(stats.copy(wisdom = it)) },
            rightLabel = stringResource(R.string.combat_charisma),
            rightValue = stats.charisma,
            onRightChange = { onStatsChange(stats.copy(charisma = it)) },
            editable = editable
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumericField(
                label = stringResource(R.string.combat_armor_class),
                value = stats.armorClass,
                editable = editable,
                onValueChange = { onStatsChange(stats.copy(armorClass = it)) },
                modifier = Modifier.weight(1f)
            )
            NumericField(
                label = stringResource(R.string.combat_hit_points),
                value = stats.hitPoints,
                editable = editable,
                onValueChange = { onStatsChange(stats.copy(hitPoints = it)) },
                modifier = Modifier.weight(1f)
            )
        }
        NpcField(
            label = stringResource(R.string.combat_challenge_rating),
            value = stats.challengeRating,
            editable = editable,
            onValueChange = { onStatsChange(stats.copy(challengeRating = it)) }
        )
    }
}

/** Two ability scores side by side, keeping the stat block compact. */
@Composable
private fun AbilityRow(
    leftLabel: String,
    leftValue: String,
    onLeftChange: (String) -> Unit,
    rightLabel: String,
    rightValue: String,
    onRightChange: (String) -> Unit,
    editable: Boolean
) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AbilityScoreField(leftLabel, leftValue, editable, onLeftChange, Modifier.weight(1f))
        AbilityScoreField(rightLabel, rightValue, editable, onRightChange, Modifier.weight(1f))
    }
}
