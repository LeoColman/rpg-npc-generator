package me.kerooker.rpgnpcgenerator.ui.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.kerooker.rpgnpcgenerator.BuildConfig
import me.kerooker.rpgnpcgenerator.R
import me.kerooker.rpgnpcgenerator.ads.RemoveAdsAction
import me.kerooker.rpgnpcgenerator.ui.theme.ThemePreference
import me.kerooker.rpgnpcgenerator.viewmodel.settings.SettingsViewModel
import org.koin.androidx.compose.koinViewModel
import java.io.IOException

private const val REPOSITORY_URL = "https://github.com/LeoColman/rpg-npc-generator"
private const val ISSUES_URL = "https://github.com/LeoColman/rpg-npc-generator/issues"

private const val EXPORT_MIME_TYPE = "application/json"
private const val EXPORT_FILE_NAME = "rpg-npc-roster.json"

// Providers label a picked backup inconsistently (often octet-stream or plain text), so accept the
// common variants rather than a single mime and let the parser reject genuinely wrong files.
private val IMPORT_MIME_TYPES = arrayOf("application/json", "application/octet-stream", "text/plain")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel = koinViewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val themePreference by viewModel.themePreference.collectAsStateWithLifecycle()
    var showThemeDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument(EXPORT_MIME_TYPE)
    ) { uri ->
        if (uri != null) scope.launch { exportRoster(context, viewModel, snackbarHostState, uri) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) scope.launch { importRoster(context, viewModel, snackbarHostState, uri) }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.nav_bar_settings)) },
                windowInsets = WindowInsets(0),
                actions = { RemoveAdsAction(snackbarHostState) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            SectionTitle(stringResource(R.string.settings_appearance_title))
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_theme_title)) },
                supportingContent = { Text(stringResource(themePreference.labelRes())) },
                leadingContent = { Icon(Icons.Filled.DarkMode, contentDescription = null) },
                modifier = Modifier.clickable { showThemeDialog = true }
            )

            SectionTitle(stringResource(R.string.settings_data_title))
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_export_title)) },
                supportingContent = { Text(stringResource(R.string.settings_export_summary)) },
                leadingContent = { Icon(Icons.Filled.Upload, contentDescription = null) },
                modifier = Modifier.clickable { exportLauncher.launch(EXPORT_FILE_NAME) }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.settings_import_title)) },
                supportingContent = { Text(stringResource(R.string.settings_import_summary)) },
                leadingContent = { Icon(Icons.Filled.Download, contentDescription = null) },
                modifier = Modifier.clickable { importLauncher.launch(IMPORT_MIME_TYPES) }
            )

            SectionTitle(stringResource(R.string.preferences_about_title))
            ListItem(
                headlineContent = { Text(stringResource(R.string.preferences_project_repository_title)) },
                supportingContent = { Text(stringResource(R.string.preferences_project_repository_summary)) },
                leadingContent = { Icon(Icons.Filled.Code, contentDescription = null) },
                modifier = Modifier.clickable { openUrl(context, REPOSITORY_URL) }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.preferences_bug_report_title)) },
                supportingContent = { Text(stringResource(R.string.preferences_bug_report_summary)) },
                leadingContent = { Icon(Icons.Filled.BugReport, contentDescription = null) },
                modifier = Modifier.clickable { openUrl(context, ISSUES_URL) }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.preferences_open_source_libs_title)) },
                supportingContent = { Text(stringResource(R.string.preferences_open_source_libs_summary)) },
                leadingContent = { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null) },
                modifier = Modifier.clickable { openUrl(context, REPOSITORY_URL) }
            )
            Text(
                text = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            )
        }
    }

    if (showThemeDialog) {
        ThemePickerDialog(
            current = themePreference,
            onSelect = {
                viewModel.setThemePreference(it)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }
}

/** Radio-button dialog letting the user pick Follow system / Light / Dark. */
@Composable
private fun ThemePickerDialog(
    current: ThemePreference,
    onSelect: (ThemePreference) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_theme_title)) },
        text = {
            Column(modifier = Modifier.selectableGroup()) {
                ThemePreference.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = option == current,
                                role = Role.RadioButton,
                                onClick = { onSelect(option) }
                            )
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = option == current, onClick = null)
                        Text(
                            text = stringResource(option.labelRes()),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = 16.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}

/** The user-facing label for each appearance option. */
@StringRes
private fun ThemePreference.labelRes(): Int = when (this) {
    ThemePreference.FOLLOW_SYSTEM -> R.string.settings_theme_follow_system
    ThemePreference.LIGHT -> R.string.settings_theme_light
    ThemePreference.DARK -> R.string.settings_theme_dark
}

/** Writes the whole roster as a backup file to [uri], reporting the count (or a failure) via snackbar. */
private suspend fun exportRoster(
    context: Context,
    viewModel: SettingsViewModel,
    snackbarHostState: SnackbarHostState,
    uri: Uri
) {
    try {
        val export = viewModel.exportJson()
        withContext(Dispatchers.IO) {
            context.contentResolver.openOutputStream(uri)?.use { it.write(export.json.toByteArray()) }
                ?: throw IOException("Could not open $uri for writing")
        }
        snackbarHostState.showSnackbar(context.getString(R.string.export_success, export.count))
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        snackbarHostState.showSnackbar(context.getString(R.string.export_error))
    }
}

/** Reads a backup file from [uri] and adds its NPCs to the roster, reporting the count (or a failure). */
private suspend fun importRoster(
    context: Context,
    viewModel: SettingsViewModel,
    snackbarHostState: SnackbarHostState,
    uri: Uri
) {
    try {
        val text = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes().decodeToString() }
                ?: throw IOException("Could not open $uri for reading")
        }
        val imported = viewModel.import(text)
        snackbarHostState.showSnackbar(context.getString(R.string.import_success, imported))
    } catch (e: CancellationException) {
        throw e
    } catch (_: Exception) {
        snackbarHostState.showSnackbar(context.getString(R.string.import_error))
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

private fun openUrl(context: Context, url: String) {
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}
