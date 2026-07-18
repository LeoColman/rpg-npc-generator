package me.kerooker.rpgnpcgenerator.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.kerooker.rpgnpcgenerator.R
import me.kerooker.rpgnpcgenerator.repository.image.PortraitServerStore
import me.kerooker.rpgnpcgenerator.repository.image.RemoteImageConfig
import org.koin.compose.koinInject

/**
 * Settings entry for the portrait-generation server. The renderer is open source and self-hostable,
 * and this lets the user point the app at their own instance (or turn portraits off by clearing the
 * password) — which is exactly what keeps the feature available in the FOSS/F-Droid build without a
 * proprietary or mandatory network service. Backed by [PortraitServerStore].
 */
@Composable
fun PortraitServerSettingsItem() {
    val store = koinInject<PortraitServerStore>()
    val scope = rememberCoroutineScope()
    val config by store.config.collectAsStateWithLifecycle(initialValue = null)
    var showDialog by remember { mutableStateOf(false) }

    ListItem(
        headlineContent = { Text(stringResource(R.string.settings_portrait_server_title)) },
        supportingContent = {
            Text(config?.baseUrl?.ifBlank { null } ?: stringResource(R.string.settings_portrait_server_summary))
        },
        leadingContent = { Icon(Icons.Filled.Image, contentDescription = null) },
        modifier = Modifier.clickable { showDialog = true }
    )

    val current = config
    if (showDialog && current != null) {
        PortraitServerDialog(
            initial = current,
            onSave = { url, user, pass ->
                scope.launch { store.save(url, user, pass) }
                showDialog = false
            },
            onReset = {
                scope.launch { store.reset() }
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun PortraitServerDialog(
    initial: RemoteImageConfig,
    onSave: (baseUrl: String, username: String, password: String) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    var baseUrl by rememberSaveable { mutableStateOf(initial.baseUrl) }
    var username by rememberSaveable { mutableStateOf(initial.username) }
    var password by rememberSaveable { mutableStateOf(initial.password) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_portrait_server_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(stringResource(R.string.portrait_server_dialog_body))
                OutlinedTextField(
                    value = baseUrl,
                    onValueChange = { baseUrl = it },
                    label = { Text(stringResource(R.string.portrait_server_url_label)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text(stringResource(R.string.portrait_server_username_label)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.portrait_server_password_label)) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(baseUrl, username, password) }) {
                Text(stringResource(R.string.portrait_server_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onReset) {
                Text(stringResource(R.string.portrait_server_reset))
            }
        }
    )
}
