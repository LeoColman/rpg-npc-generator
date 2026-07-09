package me.kerooker.rpgnpcgenerator.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import me.kerooker.rpgnpcgenerator.ui.navigation.AppRoot
import me.kerooker.rpgnpcgenerator.ui.theme.RpgNpcGeneratorTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            RpgNpcGeneratorTheme {
                AppRoot()
            }
        }
    }
}
