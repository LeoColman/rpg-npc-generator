package me.kerooker.rpgnpcgenerator.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.kerooker.rpgnpcgenerator.R

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
 * A single-line campaign field with autocomplete. While [editable], the field shows a dropdown of
 * existing [suggestions] whose text contains what the user has typed (an exact match is hidden, so
 * the menu never just echoes the current value). Picking a suggestion fills the field; the user can
 * also type a brand-new campaign name or clear it. Read-only mode renders as a plain [NpcField].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampaignField(
    label: String,
    value: String,
    suggestions: List<String>,
    editable: Boolean,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!editable) {
        NpcField(label = label, value = value, editable = false, onValueChange = {}, modifier = modifier)
        return
    }

    var expanded by remember { mutableStateOf(false) }
    val typed = value.trim()
    val matches = remember(value, suggestions) {
        suggestions.filter { it.contains(typed, ignoreCase = true) && !it.equals(typed, ignoreCase = true) }
    }
    val menuVisible = expanded && matches.isNotEmpty()

    ExposedDropdownMenuBox(
        expanded = menuVisible,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            label = { Text(label) },
            singleLine = true,
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = menuVisible,
            onDismissRequest = { expanded = false }
        ) {
            matches.forEach { suggestion ->
                DropdownMenuItem(
                    text = { Text(suggestion) },
                    onClick = {
                        onValueChange(suggestion)
                        expanded = false
                    }
                )
            }
        }
    }
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
