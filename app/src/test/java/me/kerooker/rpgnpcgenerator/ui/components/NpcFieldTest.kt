package me.kerooker.rpgnpcgenerator.ui.components

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
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
class NpcFieldTest : StringSpec({

    "displays the value and reroll invokes the callback" {
        var rerolled = false
        runAndroidComposeUiTest<ComponentActivity> {
            setContent {
                MaterialTheme {
                    NpcField(
                        label = "Race",
                        value = "Human",
                        editable = true,
                        onValueChange = {},
                        onReroll = { rerolled = true }
                    )
                }
            }

            onNodeWithText("Human").assertIsDisplayed()
            onNodeWithContentDescription("Re-roll Race").performClick()
        }

        rerolled shouldBe true
    }

    "editing the value propagates through onValueChange" {
        var current = "Human"
        runAndroidComposeUiTest<ComponentActivity> {
            setContent {
                MaterialTheme {
                    NpcField(
                        label = "Race",
                        value = current,
                        editable = true,
                        onValueChange = { current = it }
                    )
                }
            }

            onNodeWithText("Human").performTextReplacement("Elf")
        }

        current shouldBe "Elf"
    }
})
