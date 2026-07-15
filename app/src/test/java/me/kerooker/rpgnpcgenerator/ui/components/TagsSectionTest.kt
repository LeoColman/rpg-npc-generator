package me.kerooker.rpgnpcgenerator.ui.components

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runAndroidComposeUiTest
import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

@OptIn(ExperimentalTestApi::class)
@RobolectricTest(sdk = [34], application = Application::class)
class TagsSectionTest : StringSpec({

    "tapping the add-tag icon after typing appends the trimmed tag" {
        var result: List<String>? = null
        runAndroidComposeUiTest<ComponentActivity> {
            setContent {
                MaterialTheme {
                    TagsSection(
                        tags = emptyList(),
                        editable = true,
                        suggestions = emptyList(),
                        onTagsChange = { result = it }
                    )
                }
            }

            onNodeWithText("Add a tag").performTextInput("Veteran")
            onNodeWithContentDescription("Add tag").performClick()
        }

        result shouldBe listOf("Veteran")
    }

    "pressing IME done after typing appends the tag" {
        var result: List<String>? = null
        runAndroidComposeUiTest<ComponentActivity> {
            setContent {
                MaterialTheme {
                    TagsSection(
                        tags = emptyList(),
                        editable = true,
                        suggestions = emptyList(),
                        onTagsChange = { result = it }
                    )
                }
            }

            onNodeWithText("Add a tag").performTextInput("Undead")
            onNodeWithText("Undead").performImeAction()
        }

        result shouldBe listOf("Undead")
    }

    "tapping a tag's remove icon removes it" {
        var result: List<String>? = null
        runAndroidComposeUiTest<ComponentActivity> {
            setContent {
                MaterialTheme {
                    TagsSection(
                        tags = listOf("Old"),
                        editable = true,
                        suggestions = emptyList(),
                        onTagsChange = { result = it }
                    )
                }
            }

            onNodeWithContentDescription("Remove Old").performClick()
        }

        result shouldBe emptyList()
    }

    "tapping a suggestion chip adds it" {
        var result: List<String>? = null
        runAndroidComposeUiTest<ComponentActivity> {
            setContent {
                MaterialTheme {
                    TagsSection(
                        tags = emptyList(),
                        editable = true,
                        suggestions = listOf("Merchant"),
                        onTagsChange = { result = it }
                    )
                }
            }

            onNodeWithText("Merchant").performClick()
        }

        result shouldBe listOf("Merchant")
    }

    "a duplicate tag (case-insensitive) is not added again" {
        var called = false
        runAndroidComposeUiTest<ComponentActivity> {
            setContent {
                MaterialTheme {
                    TagsSection(
                        tags = listOf("Veteran"),
                        editable = true,
                        suggestions = emptyList(),
                        onTagsChange = { called = true }
                    )
                }
            }

            onNodeWithText("Add a tag").performTextInput("veteran")
            onNodeWithContentDescription("Add tag").performClick()
        }

        called shouldBe false
    }

    "blank input is ignored" {
        var called = false
        runAndroidComposeUiTest<ComponentActivity> {
            setContent {
                MaterialTheme {
                    TagsSection(
                        tags = emptyList(),
                        editable = true,
                        suggestions = emptyList(),
                        onTagsChange = { called = true }
                    )
                }
            }

            onNodeWithText("Add a tag").performTextInput("   ")
            onNodeWithContentDescription("Add tag").performClick()
        }

        called shouldBe false
    }

    "non-editable mode renders plain chips with no input or remove affordance" {
        runAndroidComposeUiTest<ComponentActivity> {
            setContent {
                MaterialTheme {
                    TagsSection(
                        tags = listOf("Merchant"),
                        editable = false,
                        suggestions = emptyList(),
                        onTagsChange = {}
                    )
                }
            }

            onNodeWithText("Merchant").assertIsDisplayed()
            onNodeWithText("Add a tag").assertDoesNotExist()
            onNodeWithContentDescription("Remove Merchant").assertDoesNotExist()
        }
    }

    "non-editable mode with no tags renders nothing" {
        runAndroidComposeUiTest<ComponentActivity> {
            setContent {
                MaterialTheme {
                    TagsSection(
                        tags = emptyList(),
                        editable = false,
                        suggestions = emptyList(),
                        onTagsChange = {}
                    )
                }
            }

            onNodeWithText("Tags").assertDoesNotExist()
        }
    }
})
