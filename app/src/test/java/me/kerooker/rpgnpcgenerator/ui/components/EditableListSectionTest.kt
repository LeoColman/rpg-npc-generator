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
class EditableListSectionTest : StringSpec({

    "editing an item's text propagates through onItemChange" {
        var edited: Pair<Int, String>? = null
        runAndroidComposeUiTest<ComponentActivity> {
            setContent {
                MaterialTheme {
                    EditableListSection(
                        title = "Languages",
                        itemLabel = "Language",
                        items = listOf("Common"),
                        editable = true,
                        onItemChange = { index, value -> edited = index to value },
                        onRemove = {},
                        onAdd = {}
                    )
                }
            }

            onNodeWithText("Common").performTextReplacement("Elvish")
        }

        edited shouldBe (0 to "Elvish")
    }

    "removing an item calls onRemove with its index" {
        var removedIndex: Int? = null
        runAndroidComposeUiTest<ComponentActivity> {
            setContent {
                MaterialTheme {
                    EditableListSection(
                        title = "Languages",
                        itemLabel = "Language",
                        items = listOf("Common"),
                        editable = true,
                        onItemChange = { _, _ -> },
                        onRemove = { removedIndex = it },
                        onAdd = {}
                    )
                }
            }

            onNodeWithContentDescription("Remove Language").performClick()
        }

        removedIndex shouldBe 0
    }

    "rerolling one item calls onReroll with its index" {
        var rerolledIndex: Int? = null
        runAndroidComposeUiTest<ComponentActivity> {
            setContent {
                MaterialTheme {
                    EditableListSection(
                        title = "Languages",
                        itemLabel = "Language",
                        items = listOf("Common"),
                        editable = true,
                        onItemChange = { _, _ -> },
                        onRemove = {},
                        onAdd = {},
                        onReroll = { rerolledIndex = it }
                    )
                }
            }

            onNodeWithContentDescription("Re-roll Language").performClick()
        }

        rerolledIndex shouldBe 0
    }

    "the reroll-all die calls onRerollAll" {
        var rerolledAll = false
        runAndroidComposeUiTest<ComponentActivity> {
            setContent {
                MaterialTheme {
                    EditableListSection(
                        title = "Languages",
                        itemLabel = "Language",
                        items = listOf("Common"),
                        editable = true,
                        onItemChange = { _, _ -> },
                        onRemove = {},
                        onAdd = {},
                        onRerollAll = { rerolledAll = true }
                    )
                }
            }

            onNodeWithContentDescription("Regenerate all Languages").performClick()
        }

        rerolledAll shouldBe true
    }

    "tapping Add Item calls onAdd" {
        var added = false
        runAndroidComposeUiTest<ComponentActivity> {
            setContent {
                MaterialTheme {
                    EditableListSection(
                        title = "Languages",
                        itemLabel = "Language",
                        items = emptyList(),
                        editable = true,
                        onItemChange = { _, _ -> },
                        onRemove = {},
                        onAdd = { added = true }
                    )
                }
            }

            onNodeWithText("Add Item").performClick()
        }

        added shouldBe true
    }

    "the lock toggle calls onToggleLock" {
        var toggled = false
        runAndroidComposeUiTest<ComponentActivity> {
            setContent {
                MaterialTheme {
                    EditableListSection(
                        title = "Languages",
                        itemLabel = "Language",
                        items = emptyList(),
                        editable = true,
                        onItemChange = { _, _ -> },
                        onRemove = {},
                        onAdd = {},
                        locked = false,
                        onToggleLock = { toggled = true }
                    )
                }
            }

            onNodeWithContentDescription("Lock Languages so it survives Randomize all").performClick()
        }

        toggled shouldBe true
    }
})
