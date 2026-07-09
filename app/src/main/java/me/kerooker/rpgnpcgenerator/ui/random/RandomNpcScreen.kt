package me.kerooker.rpgnpcgenerator.ui.random

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.kerooker.rpgnpcgenerator.R
import me.kerooker.rpgnpcgenerator.ui.components.EditableListSection
import me.kerooker.rpgnpcgenerator.ui.components.NpcField
import me.kerooker.rpgnpcgenerator.ui.components.RerollButton
import me.kerooker.rpgnpcgenerator.viewmodel.random.npc.RandomNpcViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomNpcScreen(
    viewModel: RandomNpcViewModel,
    modifier: Modifier = Modifier
) {
    val npc by viewModel.data.collectAsStateWithLifecycle()
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NpcField(
                label = stringResource(R.string.random_npc_full_name_hint),
                value = npc.name,
                editable = true,
                onValueChange = viewModel::setName,
                onReroll = viewModel::randomizeName
            )
            NpcField(
                label = stringResource(R.string.random_npc_nickname_hint),
                value = npc.nickname,
                editable = true,
                onValueChange = viewModel::setNickname,
                onReroll = viewModel::randomizeNickname
            )
            NpcField(
                label = stringResource(R.string.random_npc_race_hint),
                value = npc.race,
                editable = true,
                onValueChange = viewModel::setRace,
                onReroll = viewModel::randomizeRace
            )
            NpcField(
                label = stringResource(R.string.random_npc_age_hint),
                value = npc.age,
                editable = true,
                onValueChange = viewModel::setAge,
                onReroll = viewModel::randomizeAge
            )
            NpcField(
                label = stringResource(R.string.random_npc_gender_hint),
                value = npc.gender,
                editable = true,
                onValueChange = viewModel::setGender,
                onReroll = viewModel::randomizeGender
            )
            NpcField(
                label = stringResource(R.string.random_npc_profession_hint),
                value = npc.profession,
                editable = true,
                onValueChange = viewModel::setProfession,
                onReroll = viewModel::randomizeProfession
            )
            NpcField(
                label = stringResource(R.string.random_npc_sexuality_hint),
                value = npc.sexuality,
                editable = true,
                onValueChange = viewModel::setSexuality,
                onReroll = viewModel::randomizeSexuality
            )
            NpcField(
                label = stringResource(R.string.random_npc_alignment_hint),
                value = npc.alignment,
                editable = true,
                onValueChange = viewModel::setAlignment,
                onReroll = viewModel::randomizeAlignment
            )
            NpcField(
                label = stringResource(R.string.random_npc_motivation_hint),
                value = npc.motivation,
                editable = true,
                onValueChange = viewModel::setMotivation,
                onReroll = viewModel::randomizeMotivation
            )

            EditableListSection(
                title = stringResource(R.string.random_npc_languages_label),
                itemLabel = stringResource(R.string.random_npc_language_hint),
                items = npc.languages,
                editable = true,
                onItemChange = viewModel::setLanguage,
                onReroll = viewModel::randomizeLanguage,
                onRemove = viewModel::removeLanguage,
                onAdd = { viewModel.randomizeLanguage(npc.languages.size) }
            )

            EditableListSection(
                title = stringResource(R.string.random_npc_personality_traits_label),
                itemLabel = stringResource(R.string.random_npc_personality_hint),
                items = npc.personalityTraits,
                editable = true,
                onItemChange = viewModel::setPersonality,
                onReroll = viewModel::randomizePersonality,
                onRemove = viewModel::removePersonality,
                onAdd = { viewModel.randomizePersonality(npc.personalityTraits.size) }
            )
        }
    }
}
