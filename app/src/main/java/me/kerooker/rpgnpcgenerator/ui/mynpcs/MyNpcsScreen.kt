package me.kerooker.rpgnpcgenerator.ui.mynpcs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import me.kerooker.rpgnpcgenerator.R
import me.kerooker.rpgnpcgenerator.ads.RemoveAdsAction
import me.kerooker.rpgnpcgenerator.data.Npc
import me.kerooker.rpgnpcgenerator.viewmodel.my.npc.MyNpcsViewModel
import me.kerooker.rpgnpcgenerator.viewmodel.my.npc.NpcSortOrder
import me.kerooker.rpgnpcgenerator.viewmodel.my.npc.RosterSection
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyNpcsScreen(
    viewModel: MyNpcsViewModel,
    onNpcClick: (Long) -> Unit
) {
    val ui by viewModel.uiState.collectAsStateWithLifecycle()
    var npcPendingDeletion by remember { mutableStateOf<Npc?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.nav_bar_my_npcs)) },
                windowInsets = WindowInsets(0),
                actions = {
                    RemoveAdsAction(snackbarHostState)
                    if (ui.hasNpcs) {
                        SortMenuAction(current = ui.filter.sortOrder, onSelect = viewModel::setSortOrder)
                        GroupToggleAction(
                            grouped = ui.filter.groupByCampaign,
                            onToggle = { viewModel.setGroupByCampaign(!ui.filter.groupByCampaign) }
                        )
                    }
                }
            )
        }
    ) { padding ->
        if (!ui.hasNpcs) {
            EmptyMyNpcsContent(modifier = Modifier.padding(padding))
        } else {
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                SearchField(query = ui.filter.query, onQueryChange = viewModel::setQuery)
                if (ui.availableCampaigns.isNotEmpty()) {
                    CampaignFilterRow(
                        campaigns = ui.availableCampaigns,
                        selected = ui.filter.campaign,
                        onSelect = viewModel::setCampaignFilter
                    )
                }
                if (ui.hasResults) {
                    RosterList(
                        sections = ui.sections,
                        grouped = ui.filter.groupByCampaign,
                        onNpcClick = onNpcClick,
                        onNpcDelete = { npcPendingDeletion = it }
                    )
                } else {
                    NoResultsContent(modifier = Modifier.fillMaxSize())
                }
            }
        }
    }

    npcPendingDeletion?.let { npc ->
        AlertDialog(
            onDismissRequest = { npcPendingDeletion = null },
            title = { Text(stringResource(R.string.delete_npc_confirm_title)) },
            text = { Text(stringResource(R.string.delete_npc_confirm_message, npc.fullName)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteNpc(npc.id)
                        npcPendingDeletion = null
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { npcPendingDeletion = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.cd_clear_search))
                }
            }
        },
        placeholder = { Text(stringResource(R.string.my_npcs_search_hint)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
    )
}

@Composable
private fun CampaignFilterRow(campaigns: List<String>, selected: String?, onSelect: (String?) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selected == null,
                onClick = { onSelect(null) },
                label = { Text(stringResource(R.string.my_npcs_campaign_all)) }
            )
        }
        items(campaigns, key = { it }) { campaign ->
            FilterChip(
                selected = selected == campaign,
                onClick = { onSelect(campaign) },
                label = { Text(campaign) }
            )
        }
    }
}

@Composable
private fun SortMenuAction(current: NpcSortOrder, onSelect: (NpcSortOrder) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = stringResource(R.string.my_npcs_sort))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            SortMenuItem(
                label = stringResource(R.string.my_npcs_sort_name),
                order = NpcSortOrder.NAME_ASC,
                current = current,
                onSelect = { onSelect(it); expanded = false }
            )
            SortMenuItem(
                label = stringResource(R.string.my_npcs_sort_recent),
                order = NpcSortOrder.RECENTLY_ADDED,
                current = current,
                onSelect = { onSelect(it); expanded = false }
            )
        }
    }
}

@Composable
private fun SortMenuItem(
    label: String,
    order: NpcSortOrder,
    current: NpcSortOrder,
    onSelect: (NpcSortOrder) -> Unit
) {
    DropdownMenuItem(
        text = { Text(label) },
        onClick = { onSelect(order) },
        leadingIcon = {
            if (order == current) Icon(Icons.Filled.Check, contentDescription = null)
        }
    )
}

@Composable
private fun GroupToggleAction(grouped: Boolean, onToggle: () -> Unit) {
    IconButton(onClick = onToggle) {
        Icon(
            imageVector = Icons.Filled.Category,
            contentDescription = stringResource(R.string.my_npcs_group_by_campaign),
            tint = if (grouped) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RosterList(
    sections: List<RosterSection>,
    grouped: Boolean,
    onNpcClick: (Long) -> Unit,
    onNpcDelete: (Npc) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        sections.forEach { section ->
            if (grouped) {
                item(key = "header:${section.campaign ?: NO_CAMPAIGN_KEY}") {
                    CampaignHeader(section.campaign)
                }
            }
            items(section.npcs, key = { it.id }) { npc ->
                NpcRow(
                    npc = npc,
                    onClick = { onNpcClick(npc.id) },
                    onDeleteClick = { onNpcDelete(npc) }
                )
            }
        }
    }
}

@Composable
private fun CampaignHeader(campaign: String?) {
    Text(
        text = campaign ?: stringResource(R.string.my_npcs_no_campaign),
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 4.dp, top = 8.dp, bottom = 2.dp)
    )
}

@Composable
private fun NoResultsContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.my_npcs_no_results),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun EmptyMyNpcsContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Groups,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.my_npcs_empty_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = stringResource(R.string.my_npcs_empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun NpcRow(
    npc: Npc,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NpcAvatar(imagePath = npc.imagePath)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp)
            ) {
                Text(text = npc.fullName, style = MaterialTheme.typography.titleMedium)
                val supportingLine = listOf(npc.nickname, npc.race, npc.profession)
                    .filter { it.isNotBlank() }
                    .joinToString(" · ")
                Text(
                    text = supportingLine,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(imageVector = Icons.Filled.Delete, contentDescription = stringResource(R.string.delete))
            }
        }
    }
}

@Composable
private fun NpcAvatar(imagePath: String?) {
    if (imagePath != null) {
        AsyncImage(
            model = File(imagePath),
            contentDescription = stringResource(R.string.cd_npc_portrait),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = stringResource(R.string.cd_npc_portrait),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

private const val NO_CAMPAIGN_KEY = "__no_campaign__"
