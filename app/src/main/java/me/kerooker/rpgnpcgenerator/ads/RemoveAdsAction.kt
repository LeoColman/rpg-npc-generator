package me.kerooker.rpgnpcgenerator.ads

import androidx.activity.compose.LocalActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.kerooker.rpgnpcgenerator.R
import me.kerooker.rpgnpcgenerator.analytics.Analytics
import me.kerooker.rpgnpcgenerator.analytics.AnalyticsEvents
import org.koin.compose.koinInject

/**
 * Top-bar action to watch a rewarded ad and hide ads for a week. Renders nothing while already ad-free.
 * Follows the app's hoisted-state [AlertDialog] + snackbar conventions.
 */
@Composable
fun RemoveAdsAction(snackbarHostState: SnackbarHostState) {
    val adFreeStore = koinInject<AdFreeStore>()
    val adsEnabled by adFreeStore.adsEnabled.collectAsStateWithLifecycle(initialValue = false)
    if (!adsEnabled) return

    val rewardedAdController = koinInject<RewardedAdController>()
    val analytics = koinInject<Analytics>()
    val activity = LocalActivity.current ?: return
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }

    val successMessage = stringResource(R.string.remove_ads_success)
    val unavailableMessage = stringResource(R.string.remove_ads_unavailable)

    IconButton(onClick = { showDialog = true }) {
        Icon(Icons.Filled.Block, contentDescription = stringResource(R.string.remove_ads_action))
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.remove_ads_dialog_title)) },
            text = { Text(stringResource(R.string.remove_ads_dialog_body)) },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    rewardedAdController.show(
                        activity,
                        onReward = {
                            analytics.capture(AnalyticsEvents.AD_FREE_WEEK_GRANTED)
                            scope.launch {
                                adFreeStore.grantAdFreeWeek()
                                snackbarHostState.showSnackbar(successMessage)
                            }
                        },
                        onUnavailable = {
                            scope.launch { snackbarHostState.showSnackbar(unavailableMessage) }
                        },
                    )
                }) { Text(stringResource(R.string.remove_ads_dialog_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.remove_ads_dialog_dismiss))
                }
            },
        )
    }
}
