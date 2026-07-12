package me.kerooker.rpgnpcgenerator.ui.components

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], application = Application::class)
class NpcFieldTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `displays the value and reroll invokes the callback`() {
        var rerolled = false
        composeRule.setContent {
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

        composeRule.onNodeWithText("Human").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Re-roll Race").performClick()

        rerolled shouldBe true
    }

    @Test
    fun `editing the value propagates through onValueChange`() {
        var current = "Human"
        composeRule.setContent {
            MaterialTheme {
                NpcField(
                    label = "Race",
                    value = current,
                    editable = true,
                    onValueChange = { current = it }
                )
            }
        }

        composeRule.onNodeWithText("Human").performTextReplacement("Elf")

        current shouldBe "Elf"
    }
}
