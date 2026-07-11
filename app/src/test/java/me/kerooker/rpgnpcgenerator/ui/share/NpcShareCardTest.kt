package me.kerooker.rpgnpcgenerator.ui.share

import android.app.Application
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import me.kerooker.rpgnpcgenerator.data.Npc
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], application = Application::class)
class NpcShareCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val labels = NpcShareCardLabels(
        profession = "Profession",
        alignment = "Alignment",
        motivation = "Motivation",
        personality = "Personality"
    )

    private fun sampleNpc(
        nickname: String = "The Swift",
        personalityTraits: List<String> = listOf("Brave", "Curious")
    ) = Npc(
        id = 0,
        fullName = "Aria Nightsong",
        nickname = nickname,
        gender = "Female",
        sexuality = "Heterosexual",
        race = "Human",
        age = "Adult",
        profession = "Blacksmith",
        motivation = "Protect the forest",
        alignment = "Neutral Good",
        personalityTraits = personalityTraits,
        languages = listOf("Common"),
        imagePath = null,
        notes = ""
    )

    @Test
    fun `renders the npc key attributes and watermark`() {
        composeRule.setContent {
            MaterialTheme {
                NpcShareCard(
                    npc = sampleNpc(),
                    portrait = null,
                    footer = "Created with RPG NPC Generator",
                    labels = labels
                )
            }
        }

        // The full card can be taller than the test viewport, so assert the attributes are composed
        // into the card (exist in the semantics tree) rather than requiring them to be on-screen.
        composeRule.onNodeWithText("Aria Nightsong").assertExists()
        composeRule.onNodeWithText("The Swift", substring = true).assertExists()
        composeRule.onNodeWithText("Human · Adult · Female").assertExists()
        composeRule.onNodeWithText("Blacksmith").assertExists()
        composeRule.onNodeWithText("Neutral Good").assertExists()
        composeRule.onNodeWithText("Protect the forest").assertExists()
        composeRule.onNodeWithText("Brave").assertExists()
        composeRule.onNodeWithText("Curious").assertExists()
        composeRule.onNodeWithText("Created with RPG NPC Generator").assertExists()
    }

    @Test
    fun `omits the nickname line when the npc has no nickname`() {
        composeRule.setContent {
            MaterialTheme {
                NpcShareCard(
                    npc = sampleNpc(nickname = ""),
                    portrait = null,
                    footer = "Created with RPG NPC Generator",
                    labels = labels
                )
            }
        }

        composeRule.onNodeWithText("Aria Nightsong").assertExists()
        composeRule.onNodeWithText("“", substring = true).assertDoesNotExist()
    }
}
