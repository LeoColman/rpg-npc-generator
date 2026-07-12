package me.kerooker.rpgnpcgenerator.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.kerooker.rpgnpcgenerator.ads.ConsentManager
import me.kerooker.rpgnpcgenerator.ads.RewardedAdController
import me.kerooker.rpgnpcgenerator.ui.navigation.AppRoot
import me.kerooker.rpgnpcgenerator.ui.theme.RpgNpcGeneratorTheme
import me.kerooker.rpgnpcgenerator.ui.theme.ThemePreference
import me.kerooker.rpgnpcgenerator.ui.theme.ThemePreferenceStore
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {

    private val consentManager: ConsentManager by inject()
    private val rewardedAdController: RewardedAdController by inject()
    private val themePreferenceStore: ThemePreferenceStore by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // Gather UMP consent, initialize the Ads SDK, then warm up a rewarded ad for the remove-ads action.
        consentManager.ensureConsentAndInit(this) { rewardedAdController.preload() }
        setContent {
            val themePreference by themePreferenceStore.themePreference
                .collectAsStateWithLifecycle(initialValue = ThemePreference.DEFAULT)
            val darkTheme = when (themePreference) {
                ThemePreference.FOLLOW_SYSTEM -> isSystemInDarkTheme()
                ThemePreference.LIGHT -> false
                ThemePreference.DARK -> true
            }
            RpgNpcGeneratorTheme(darkTheme = darkTheme) {
                AppRoot()
            }
        }
    }
}
