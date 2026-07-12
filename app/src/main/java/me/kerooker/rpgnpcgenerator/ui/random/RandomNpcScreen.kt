package me.kerooker.rpgnpcgenerator.ui.random

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.kerooker.rpgnpcgenerator.R
import me.kerooker.rpgnpcgenerator.ads.RemoveAdsAction
import me.kerooker.rpgnpcgenerator.ui.components.CombatStatsSection
import me.kerooker.rpgnpcgenerator.ui.components.EditableListSection
import me.kerooker.rpgnpcgenerator.ui.components.FieldGroup
import me.kerooker.rpgnpcgenerator.ui.components.NpcField
import me.kerooker.rpgnpcgenerator.ui.components.RerollButton
import me.kerooker.rpgnpcgenerator.ui.components.toUi
import me.kerooker.rpgnpcgenerator.viewmodel.random.npc.LockableField
import me.kerooker.rpgnpcgenerator.viewmodel.random.npc.PortraitUiState
import me.kerooker.rpgnpcgenerator.viewmodel.random.npc.RandomNpcViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomNpcScreen(
    viewModel: RandomNpcViewModel,
    modifier: Modifier = Modifier
) {
    val npc by viewModel.data.collectAsStateWithLifecycle()
    val portrait by viewModel.portrait.collectAsStateWithLifecycle()
    val locks by viewModel.lockedFields.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val savedMessage = stringResource(R.string.npc_saved)

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.nav_bar_random_npc)) },
                windowInsets = WindowInsets(0),
                actions = {
                    RemoveAdsAction(snackbarHostState)
                    RerollButton(
                        contentDescription = stringResource(R.string.randomize_all),
                        onClick = viewModel::randomizeAll
                    )
                    IconButton(
                        onClick = {
                            viewModel.saveCurrentNpc()
                            scope.launch { snackbarHostState.showSnackbar(savedMessage) }
                        }
                    ) {
                        Icon(Icons.Filled.Save, contentDescription = stringResource(R.string.save))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            RandomizePortrait(portrait)

            FieldGroup(title = stringResource(R.string.group_identity)) {
                NpcField(
                    label = stringResource(R.string.random_npc_full_name_hint),
                    value = npc.name,
                    editable = true,
                    onValueChange = viewModel::setName,
                    onReroll = viewModel::randomizeName,
                    locked = LockableField.NAME in locks,
                    onToggleLock = { viewModel.toggleLock(LockableField.NAME) }
                )
                NpcField(
                    label = stringResource(R.string.random_npc_nickname_hint),
                    value = npc.nickname,
                    editable = true,
                    onValueChange = viewModel::setNickname,
                    onReroll = viewModel::randomizeNickname,
                    locked = LockableField.NICKNAME in locks,
                    onToggleLock = { viewModel.toggleLock(LockableField.NICKNAME) }
                )
            }

            FieldGroup(title = stringResource(R.string.group_characteristics)) {
                NpcField(
                    label = stringResource(R.string.random_npc_race_hint),
                    value = npc.race,
                    editable = true,
                    onValueChange = viewModel::setRace,
                    onReroll = viewModel::randomizeRace,
                    locked = LockableField.RACE in locks,
                    onToggleLock = { viewModel.toggleLock(LockableField.RACE) }
                )
                NpcField(
                    label = stringResource(R.string.random_npc_age_hint),
                    value = npc.age,
                    editable = true,
                    onValueChange = viewModel::setAge,
                    onReroll = viewModel::randomizeAge,
                    locked = LockableField.AGE in locks,
                    onToggleLock = { viewModel.toggleLock(LockableField.AGE) }
                )
                NpcField(
                    label = stringResource(R.string.random_npc_gender_hint),
                    value = npc.gender,
                    editable = true,
                    onValueChange = viewModel::setGender,
                    onReroll = viewModel::randomizeGender,
                    locked = LockableField.GENDER in locks,
                    onToggleLock = { viewModel.toggleLock(LockableField.GENDER) }
                )
                NpcField(
                    label = stringResource(R.string.random_npc_sexuality_hint),
                    value = npc.sexuality,
                    editable = true,
                    onValueChange = viewModel::setSexuality,
                    onReroll = viewModel::randomizeSexuality,
                    locked = LockableField.SEXUALITY in locks,
                    onToggleLock = { viewModel.toggleLock(LockableField.SEXUALITY) }
                )
            }

            FieldGroup(title = stringResource(R.string.group_background)) {
                NpcField(
                    label = stringResource(R.string.random_npc_profession_hint),
                    value = npc.profession,
                    editable = true,
                    onValueChange = viewModel::setProfession,
                    onReroll = viewModel::randomizeProfession,
                    locked = LockableField.PROFESSION in locks,
                    onToggleLock = { viewModel.toggleLock(LockableField.PROFESSION) }
                )
                NpcField(
                    label = stringResource(R.string.random_npc_alignment_hint),
                    value = npc.alignment,
                    editable = true,
                    onValueChange = viewModel::setAlignment,
                    onReroll = viewModel::randomizeAlignment,
                    locked = LockableField.ALIGNMENT in locks,
                    onToggleLock = { viewModel.toggleLock(LockableField.ALIGNMENT) }
                )
                NpcField(
                    label = stringResource(R.string.random_npc_motivation_hint),
                    value = npc.motivation,
                    editable = true,
                    onValueChange = viewModel::setMotivation,
                    onReroll = viewModel::randomizeMotivation,
                    locked = LockableField.MOTIVATION in locks,
                    onToggleLock = { viewModel.toggleLock(LockableField.MOTIVATION) }
                )
            }

            EditableListSection(
                title = stringResource(R.string.random_npc_languages_label),
                itemLabel = stringResource(R.string.random_npc_language_hint),
                items = npc.languages,
                editable = true,
                onItemChange = viewModel::setLanguage,
                onReroll = viewModel::randomizeLanguage,
                onRemove = viewModel::removeLanguage,
                onAdd = { viewModel.randomizeLanguage(npc.languages.size) },
                locked = LockableField.LANGUAGES in locks,
                onToggleLock = { viewModel.toggleLock(LockableField.LANGUAGES) }
            )

            EditableListSection(
                title = stringResource(R.string.random_npc_personality_traits_label),
                itemLabel = stringResource(R.string.random_npc_personality_hint),
                items = npc.personalityTraits,
                editable = true,
                onItemChange = viewModel::setPersonality,
                onReroll = viewModel::randomizePersonality,
                onRemove = viewModel::removePersonality,
                onAdd = { viewModel.randomizePersonality(npc.personalityTraits.size) },
                onRerollAll = viewModel::randomizeAllPersonalities,
                locked = LockableField.PERSONALITY in locks,
                onToggleLock = { viewModel.toggleLock(LockableField.PERSONALITY) }
            )

            // Fields are read-only here (combat isn't hand-edited on this screen), but the whole
            // block can be re-rolled at once: combat values are not part of the portrait prompt, so
            // this never triggers the reactive portrait-request/debounce path.
            npc.combat?.let {
                CombatStatsSection(
                    stats = it.toUi(),
                    editable = false,
                    onReroll = viewModel::randomizeCombatStats,
                    locked = LockableField.COMBAT in locks,
                    onToggleLock = { viewModel.toggleLock(LockableField.COMBAT) }
                )
            }
        }
    }
}

/** Portrait for the NPC being rolled: the render (or a placeholder), a spinner, and queue/ETA text. */
@Composable
private fun RandomizePortrait(state: PortraitUiState) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .width(PORTRAIT_WIDTH)
                .aspectRatio(PORTRAIT_ASPECT)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            val bitmap = state.bitmap
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
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
            if (state.isGenerating) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = SCRIM_ALPHA)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onSecondaryContainer)
                }
            }
        }
        PortraitCaption(state)
    }
}

/** The "Queue number: X · ETA: Y" line (or generating/failed text) shown under the portrait. */
@Composable
private fun PortraitCaption(state: PortraitUiState) {
    when {
        state.failed -> Text(
            text = stringResource(R.string.random_npc_portrait_failed),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )
        state.isGenerating && state.queueNumber != null ->
            QueueCaption(queueNumber = state.queueNumber, etaSeconds = state.etaSeconds)
        state.isGenerating -> Text(
            text = stringResource(R.string.random_npc_portrait_generating),
            style = MaterialTheme.typography.bodySmall
        )
    }
}

/**
 * The queue/ETA line. At queue position 0 the render is running now, so the ETA becomes a live
 * one-second countdown; if it overruns the estimate we drop the misleading number and just say it's
 * generating. For positions ahead in line it stays a static estimate.
 */
@Composable
private fun QueueCaption(queueNumber: Int, etaSeconds: Int?) {
    val text = if (queueNumber == 0 && etaSeconds != null) {
        var remaining by remember(etaSeconds) { mutableIntStateOf(etaSeconds) }
        LaunchedEffect(etaSeconds) {
            while (remaining > 0) {
                delay(COUNTDOWN_TICK_MS)
                remaining -= 1
            }
        }
        if (remaining > 0) {
            stringResource(R.string.random_npc_portrait_queue, queueNumber, formatEta(remaining))
        } else {
            stringResource(R.string.random_npc_portrait_generating)
        }
    } else {
        stringResource(R.string.random_npc_portrait_queue, queueNumber, formatEta(etaSeconds))
    }
    Text(text = text, style = MaterialTheme.typography.bodySmall)
}

private fun formatEta(seconds: Int?): String {
    val total = seconds ?: return ""
    return if (total < SECONDS_IN_MINUTE) {
        "${total}s"
    } else {
        "${total / SECONDS_IN_MINUTE}m ${total % SECONDS_IN_MINUTE}s"
    }
}

private val PORTRAIT_WIDTH = 200.dp
private const val PORTRAIT_ASPECT = 512f / 640f
private const val SCRIM_ALPHA = 0.35f
private const val SECONDS_IN_MINUTE = 60
private const val COUNTDOWN_TICK_MS = 1_000L
