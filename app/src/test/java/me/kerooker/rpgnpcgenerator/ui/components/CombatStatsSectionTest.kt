package me.kerooker.rpgnpcgenerator.ui.components

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.runAndroidComposeUiTest
import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

@OptIn(ExperimentalTestApi::class)
@RobolectricTest(sdk = [34], application = Application::class)
class CombatStatsSectionTest : StringSpec({

    "the reroll-all die calls onReroll" {
        var rerolled = false
        runAndroidComposeUiTest<ComponentActivity> {
            setContent {
                MaterialTheme {
                    CombatStatsSection(
                        stats = CombatStatsUi(),
                        editable = true,
                        onReroll = { rerolled = true }
                    )
                }
            }

            onNodeWithContentDescription("Regenerate all Combat stats").performClick()
        }

        rerolled shouldBe true
    }

    "the lock toggle calls onToggleLock" {
        var toggled = false
        runAndroidComposeUiTest<ComponentActivity> {
            setContent {
                MaterialTheme {
                    CombatStatsSection(
                        stats = CombatStatsUi(),
                        editable = true,
                        locked = false,
                        onToggleLock = { toggled = true }
                    )
                }
            }

            onNodeWithContentDescription("Lock Combat stats so it survives Randomize all").performClick()
        }

        toggled shouldBe true
    }

    "editing the strength field propagates the updated stats" {
        var changed: CombatStatsUi? = null
        runAndroidComposeUiTest<ComponentActivity> {
            setContent {
                MaterialTheme {
                    CombatStatsSection(
                        stats = CombatStatsUi(),
                        editable = true,
                        onStatsChange = { changed = it }
                    )
                }
            }

            onNodeWithText("STR").performTextReplacement("14")
        }

        changed shouldBe CombatStatsUi(strength = "14")
    }

    "editing the challenge rating field propagates the updated stats" {
        var changed: CombatStatsUi? = null
        runAndroidComposeUiTest<ComponentActivity> {
            setContent {
                MaterialTheme {
                    CombatStatsSection(
                        stats = CombatStatsUi(),
                        editable = true,
                        onStatsChange = { changed = it }
                    )
                }
            }

            onNodeWithText("Challenge Rating").performTextReplacement("5")
        }

        changed shouldBe CombatStatsUi(challengeRating = "5")
    }
})
