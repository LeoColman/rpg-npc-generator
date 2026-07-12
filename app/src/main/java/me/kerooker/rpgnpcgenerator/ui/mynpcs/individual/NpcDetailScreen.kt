package me.kerooker.rpgnpcgenerator.ui.mynpcs.individual

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.kerooker.rpgnpcgenerator.R
import me.kerooker.rpgnpcgenerator.data.Npc
import me.kerooker.rpgnpcgenerator.ui.components.CampaignField
import me.kerooker.rpgnpcgenerator.ui.components.CombatStatsSection
import me.kerooker.rpgnpcgenerator.ui.components.CombatStatsUi
import me.kerooker.rpgnpcgenerator.ui.components.EditableListSection
import me.kerooker.rpgnpcgenerator.ui.components.NpcField
import me.kerooker.rpgnpcgenerator.ui.components.TagsSection
import me.kerooker.rpgnpcgenerator.ui.share.NpcExportFormat
import me.kerooker.rpgnpcgenerator.ui.share.NpcExportRequest
import me.kerooker.rpgnpcgenerator.ui.share.NpcShareCardCapture
import me.kerooker.rpgnpcgenerator.ui.util.ImageStore
import me.kerooker.rpgnpcgenerator.viewmodel.my.npc.individual.EditState
import me.kerooker.rpgnpcgenerator.viewmodel.my.npc.individual.IndividualNpcViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NpcDetailScreen(
    viewModel: IndividualNpcViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val npc by viewModel.npc.collectAsStateWithLifecycle()
    val editState by viewModel.editState.collectAsStateWithLifecycle()
    val campaignSuggestions by viewModel.campaignSuggestions.collectAsStateWithLifecycle()
    val tags by viewModel.tags.collectAsStateWithLifecycle()
    val tagSuggestions by viewModel.tagSuggestions.collectAsStateWithLifecycle()
    val isEditing = editState == EditState.EDIT
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    // The NPC + chosen format currently being rendered off-screen; null when no export is in flight.
    var exportRequest by remember { mutableStateOf<NpcExportRequest?>(null) }

    // The editable working copy. Reset to the persisted value whenever we are not editing. A portrait
    // generated in the background lands on the persisted NPC, so it flows in here via [npc].
    var draft by remember { mutableStateOf<Npc?>(null) }
    LaunchedEffect(npc, isEditing) {
        if (!isEditing) draft = npc
    }

    // Editable tags working copy, likewise reset from the persisted tags whenever we are not editing.
    var draftTags by remember { mutableStateOf<List<String>>(emptyList()) }
    LaunchedEffect(tags, isEditing) {
        if (!isEditing) draftTags = tags
    }

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                title = { Text(npc?.fullName.orEmpty()) },
                windowInsets = WindowInsets(0),
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                actions = {
                    if (isEditing) {
                        IconButton(onClick = viewModel::cancelEdit) {
                            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.individual_npc_cancel))
                        }
                        IconButton(onClick = { draft?.let { viewModel.saveEdit(it, draftTags) } }) {
                            Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.individual_npc_save))
                        }
                    } else {
                        IconButton(onClick = { if (npc != null) showExportDialog = true }) {
                            Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.export_npc))
                        }
                        IconButton(onClick = viewModel::enableEdit) {
                            Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.individual_npc_edit))
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.delete))
                        }
                    }
                }
            )
        }
    ) { padding ->
        val editing = draft
        if (editing == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            NpcDetailContent(
                draft = editing,
                isEditing = isEditing,
                campaignSuggestions = campaignSuggestions,
                tags = draftTags,
                tagSuggestions = tagSuggestions,
                contentPadding = padding,
                onGeneratePortrait = viewModel::generatePortrait,
                onDraftChange = { draft = it },
                onTagsChange = { draftTags = it }
            )
        }

        // Off-screen: renders the export sheet and fires the share sheet, then resets. Draws nothing
        // visible, so it can live alongside the detail content in the Scaffold's content slot.
        NpcShareCardCapture(
            request = exportRequest,
            onFinished = { exportRequest = null }
        )
    }

    if (showExportDialog) {
        ExportFormatDialog(
            onPick = { format ->
                showExportDialog = false
                npc?.let { exportRequest = NpcExportRequest(it, format) }
            },
            onDismiss = { showExportDialog = false }
        )
    }

    if (showDeleteDialog) {
        DeleteDialog(
            npcName = npc?.fullName.orEmpty(),
            onConfirm = {
                showDeleteDialog = false
                viewModel.delete()
                onBack()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun DeleteDialog(npcName: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_npc_confirm_title)) },
        text = { Text(stringResource(R.string.delete_npc_confirm_message, npcName)) },
        confirmButton = {
            TextButton(onClick = onConfirm) { Text(stringResource(R.string.delete)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

/** Lets the user pick which file format to export the NPC sheet as before the share sheet appears. */
@Composable
private fun ExportFormatDialog(onPick: (NpcExportFormat) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.export_npc)) },
        text = {
            Column {
                TextButton(
                    onClick = { onPick(NpcExportFormat.PNG) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.export_format_png))
                }
                TextButton(
                    onClick = { onPick(NpcExportFormat.PDF) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.export_format_pdf))
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

@Suppress("LongMethod")
@Composable
private fun NpcDetailContent(
    draft: Npc,
    isEditing: Boolean,
    campaignSuggestions: List<String>,
    tags: List<String>,
    tagSuggestions: List<String>,
    contentPadding: PaddingValues,
    onGeneratePortrait: () -> Unit,
    onDraftChange: (Npc) -> Unit,
    onTagsChange: (List<String>) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val pickPortrait = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val path = withContext(Dispatchers.IO) { ImageStore.persistPortrait(context, uri) }
                if (path != null) onDraftChange(draft.copy(imagePath = path))
            }
        }
    }
    val notificationPermission =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val queuedMessage = stringResource(R.string.portrait_queued_toast)
    val queuePortrait: () -> Unit = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        onGeneratePortrait()
        Toast.makeText(context, queuedMessage, Toast.LENGTH_LONG).show()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Portrait(
            imagePath = draft.imagePath,
            editable = isEditing,
            onPick = {
                pickPortrait.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        )

        // Fire-and-forget: renders on the server queue and notifies when ready. View mode only,
        // since the result attaches to the saved NPC (the user can leave and come back).
        if (!isEditing) {
            OutlinedButton(onClick = queuePortrait) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(
                    text = stringResource(R.string.individual_npc_generate_portrait),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        NpcField(
            label = stringResource(R.string.random_npc_full_name_hint),
            value = draft.fullName,
            editable = isEditing,
            onValueChange = { onDraftChange(draft.copy(fullName = it)) }
        )
        NpcField(
            label = stringResource(R.string.random_npc_nickname_hint),
            value = draft.nickname,
            editable = isEditing,
            onValueChange = { onDraftChange(draft.copy(nickname = it)) }
        )
        NpcField(
            label = stringResource(R.string.individual_npc_profession_hint),
            value = draft.profession,
            editable = isEditing,
            onValueChange = { onDraftChange(draft.copy(profession = it)) }
        )
        NpcField(
            label = stringResource(R.string.individual_npc_race_hint),
            value = draft.race,
            editable = isEditing,
            onValueChange = { onDraftChange(draft.copy(race = it)) }
        )
        NpcField(
            label = stringResource(R.string.individual_npc_gender_hint),
            value = draft.gender,
            editable = isEditing,
            onValueChange = { onDraftChange(draft.copy(gender = it)) }
        )
        NpcField(
            label = stringResource(R.string.individual_npc_age_hint),
            value = draft.age,
            editable = isEditing,
            onValueChange = { onDraftChange(draft.copy(age = it)) }
        )
        NpcField(
            label = stringResource(R.string.individual_npc_sexuality_hint),
            value = draft.sexuality,
            editable = isEditing,
            onValueChange = { onDraftChange(draft.copy(sexuality = it)) }
        )
        NpcField(
            label = stringResource(R.string.individual_npc_alignment_hint),
            value = draft.alignment,
            editable = isEditing,
            onValueChange = { onDraftChange(draft.copy(alignment = it)) }
        )
        NpcField(
            label = stringResource(R.string.individual_npc_motivation_hint),
            value = draft.motivation,
            editable = isEditing,
            onValueChange = { onDraftChange(draft.copy(motivation = it)) }
        )
        CampaignField(
            label = stringResource(R.string.individual_npc_campaign_hint),
            value = draft.campaign.orEmpty(),
            suggestions = campaignSuggestions,
            editable = isEditing,
            onValueChange = { onDraftChange(draft.copy(campaign = it.ifBlank { null })) }
        )

        TagsSection(
            tags = tags,
            editable = isEditing,
            suggestions = tagSuggestions,
            onTagsChange = onTagsChange
        )

        EditableListSection(
            title = stringResource(R.string.individual_npc_languages_label),
            itemLabel = stringResource(R.string.individual_npc_language_hint),
            items = draft.languages,
            editable = isEditing,
            onItemChange = { index, value ->
                onDraftChange(draft.copy(languages = draft.languages.toMutableList().also { it[index] = value }))
            },
            onRemove = { index ->
                onDraftChange(draft.copy(languages = draft.languages.toMutableList().also { it.removeAt(index) }))
            },
            onAdd = { onDraftChange(draft.copy(languages = draft.languages + "")) }
        )

        EditableListSection(
            title = stringResource(R.string.individual_npc_personality_label),
            itemLabel = stringResource(R.string.individual_npc_personality_hint),
            items = draft.personalityTraits,
            editable = isEditing,
            onItemChange = { index, value ->
                onDraftChange(
                    draft.copy(personalityTraits = draft.personalityTraits.toMutableList().also { it[index] = value })
                )
            },
            onRemove = { index ->
                onDraftChange(
                    draft.copy(personalityTraits = draft.personalityTraits.toMutableList().also { it.removeAt(index) })
                )
            },
            onAdd = { onDraftChange(draft.copy(personalityTraits = draft.personalityTraits + "")) }
        )

        EditableListSection(
            title = stringResource(R.string.individual_npc_items_label),
            itemLabel = stringResource(R.string.individual_npc_item_hint),
            items = draft.items,
            editable = isEditing,
            onItemChange = { index, value ->
                onDraftChange(draft.copy(items = draft.items.toMutableList().also { it[index] = value }))
            },
            onRemove = { index ->
                onDraftChange(draft.copy(items = draft.items.toMutableList().also { it.removeAt(index) }))
            },
            onAdd = { onDraftChange(draft.copy(items = draft.items + "")) }
        )

        // Optional D&D 5e combat block. Hidden in view mode for NPCs saved before this existed, but
        // always shown while editing so stats can be filled in.
        if (isEditing || draft.hasCombatStats()) {
            CombatStatsSection(
                stats = draft.toCombatStatsUi(),
                editable = isEditing,
                onStatsChange = { onDraftChange(draft.withCombatStats(it)) }
            )
        }

        OutlinedTextField(
            value = draft.notes,
            onValueChange = { onDraftChange(draft.copy(notes = it)) },
            label = { Text(stringResource(R.string.individual_npc_notes_hint)) },
            readOnly = !isEditing,
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Portrait(imagePath: String?, editable: Boolean, onPick: () -> Unit) {
    // Rounded portrait card sized to the generated art's aspect, so the whole figure shows uncropped.
    Box(modifier = Modifier.width(PORTRAIT_WIDTH).aspectRatio(PORTRAIT_ASPECT)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (imagePath != null) {
                AsyncImage(
                    model = File(imagePath),
                    contentDescription = stringResource(R.string.cd_npc_portrait),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Person,
                    contentDescription = stringResource(R.string.cd_npc_portrait),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(72.dp)
                )
            }
        }
        if (editable) {
            IconButton(
                onClick = onPick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 8.dp, y = 8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = stringResource(R.string.cd_change_portrait),
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/** Reads the row's nullable combat columns as editable-text UI values (blank when unset). */
private fun Npc.toCombatStatsUi() = CombatStatsUi(
    strength = strength?.toString().orEmpty(),
    dexterity = dexterity?.toString().orEmpty(),
    constitution = constitution?.toString().orEmpty(),
    intelligence = intelligence?.toString().orEmpty(),
    wisdom = wisdom?.toString().orEmpty(),
    charisma = charisma?.toString().orEmpty(),
    armorClass = armorClass?.toString().orEmpty(),
    hitPoints = hitPoints?.toString().orEmpty(),
    challengeRating = challengeRating.orEmpty()
)

/** Folds edited combat text back into the row; blank fields become null so they stay "unset". */
private fun Npc.withCombatStats(ui: CombatStatsUi) = copy(
    strength = ui.strength.toLongOrNull(),
    dexterity = ui.dexterity.toLongOrNull(),
    constitution = ui.constitution.toLongOrNull(),
    intelligence = ui.intelligence.toLongOrNull(),
    wisdom = ui.wisdom.toLongOrNull(),
    charisma = ui.charisma.toLongOrNull(),
    armorClass = ui.armorClass.toLongOrNull(),
    hitPoints = ui.hitPoints.toLongOrNull(),
    challengeRating = ui.challengeRating.ifBlank { null }
)

/** True when this NPC carries any combat stat (older NPCs predate the block and carry none). */
private fun Npc.hasCombatStats(): Boolean =
    listOf(strength, dexterity, constitution, intelligence, wisdom, charisma, armorClass, hitPoints)
        .any { it != null } || !challengeRating.isNullOrBlank()

private val PORTRAIT_WIDTH = 200.dp
private const val PORTRAIT_ASPECT = 512f / 640f
