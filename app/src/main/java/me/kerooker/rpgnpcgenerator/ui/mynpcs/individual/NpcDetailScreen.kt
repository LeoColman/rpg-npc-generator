package me.kerooker.rpgnpcgenerator.ui.mynpcs.individual

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.kerooker.rpgnpcgenerator.R
import me.kerooker.rpgnpcgenerator.data.Npc
import me.kerooker.rpgnpcgenerator.ui.components.EditableListSection
import me.kerooker.rpgnpcgenerator.ui.components.NpcField
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
    val isEditing = editState == EditState.EDIT
    var showDeleteDialog by remember { mutableStateOf(false) }

    // The editable working copy. Reset to the persisted value whenever we are not editing.
    var draft by remember { mutableStateOf<Npc?>(null) }
    LaunchedEffect(npc, isEditing) {
        if (!isEditing) draft = npc
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
                        IconButton(onClick = { draft?.let(viewModel::saveEdit) }) {
                            Icon(Icons.Filled.Check, contentDescription = stringResource(R.string.individual_npc_save))
                        }
                    } else {
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
                contentPadding = padding,
                onDraftChange = { draft = it }
            )
        }
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

@Suppress("LongMethod")
@Composable
private fun NpcDetailContent(
    draft: Npc,
    isEditing: Boolean,
    contentPadding: PaddingValues,
    onDraftChange: (Npc) -> Unit
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
            onClick = {
                pickPortrait.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        )

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
private fun Portrait(imagePath: String?, editable: Boolean, onClick: () -> Unit) {
    var boxModifier = Modifier
        .size(160.dp)
        .clip(CircleShape)
        .background(MaterialTheme.colorScheme.secondaryContainer)
    if (editable) boxModifier = boxModifier.clickable(onClick = onClick)

    Box(modifier = boxModifier, contentAlignment = Alignment.Center) {
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
        if (editable) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Edit,
                    contentDescription = stringResource(R.string.cd_change_portrait),
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }
    }
}
