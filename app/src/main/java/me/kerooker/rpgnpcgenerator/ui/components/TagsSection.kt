package me.kerooker.rpgnpcgenerator.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import me.kerooker.rpgnpcgenerator.R

/**
 * The "Tags" block on the NPC detail screen. In view mode it renders the NPC's [tags] as read-only
 * chips (and shows nothing when there are none). While [editable], each chip carries a remove
 * affordance, a text field appends a new tag, and matching [suggestions] (existing roster tags not
 * yet applied) are offered as tap-to-add chips. Duplicates (case-insensitive) and blanks are ignored.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TagsSection(
    tags: List<String>,
    editable: Boolean,
    suggestions: List<String>,
    onTagsChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!editable && tags.isEmpty()) return

    var input by remember { mutableStateOf("") }

    fun addTag(raw: String) {
        val trimmed = raw.trim()
        if (trimmed.isNotEmpty() && tags.none { it.equals(trimmed, ignoreCase = true) }) {
            onTagsChange(tags + trimmed)
        }
        input = ""
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.individual_npc_tags_label),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        if (tags.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tags.forEach { tag ->
                    if (editable) {
                        InputChip(
                            selected = false,
                            onClick = { onTagsChange(tags - tag) },
                            label = { Text(tag) },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Filled.Close,
                                    contentDescription = stringResource(R.string.cd_remove_item, tag),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        )
                    } else {
                        AssistChip(onClick = {}, label = { Text(tag) })
                    }
                }
            }
        }

        if (editable) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text(stringResource(R.string.individual_npc_tag_hint)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { addTag(input) }),
                trailingIcon = {
                    IconButton(onClick = { addTag(input) }) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = stringResource(R.string.individual_npc_add_tag)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            val typed = input.trim()
            val available = suggestions.filter { suggestion ->
                tags.none { it.equals(suggestion, ignoreCase = true) } &&
                    suggestion.contains(typed, ignoreCase = true)
            }
            if (available.isNotEmpty()) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    available.forEach { suggestion ->
                        SuggestionChip(
                            onClick = { addTag(suggestion) },
                            label = { Text(suggestion) },
                            modifier = Modifier.padding(top = 0.dp)
                        )
                    }
                }
            }
        }
    }
}
