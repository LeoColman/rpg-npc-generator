package me.kerooker.rpgnpcgenerator.ui.components

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
import br.com.colman.kotest.FunSpec
import br.com.colman.kotest.KotestRunnerAndroid
import io.kotest.matchers.shouldBe
import org.junit.runner.RunWith

@OptIn(ExperimentalTestApi::class)
@RunWith(KotestRunnerAndroid::class)
class TagsSectionTest : FunSpec({

    test("tapping the add-tag icon after typing appends the trimmed tag") {
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

    test("pressing IME done after typing appends the tag") {
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

    test("tapping a tag's remove icon removes it") {
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

    test("tapping a suggestion chip adds it") {
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

    test("a duplicate tag (case-insensitive) is not added again") {
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

    test("blank input is ignored") {
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

    test("non-editable mode renders plain chips with no input or remove affordance") {
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

    test("non-editable mode with no tags renders nothing") {
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
