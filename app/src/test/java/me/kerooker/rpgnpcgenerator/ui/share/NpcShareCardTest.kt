package me.kerooker.rpgnpcgenerator.ui.share

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runAndroidComposeUiTest
import br.com.colman.kotest.android.extensions.robolectric.RobolectricTest
import io.kotest.core.spec.style.StringSpec
import me.kerooker.rpgnpcgenerator.data.Npc

private val labels = NpcShareCardLabels(
    profession = "Profession",
    alignment = "Alignment",
    motivation = "Motivation",
    personality = "Personality",
    languages = "Languages",
    items = "Items",
    combat = CombatSheetLabels(
        title = "Combat stats",
        strength = "STR",
        dexterity = "DEX",
        constitution = "CON",
        intelligence = "INT",
        wisdom = "WIS",
        charisma = "CHA",
        armorClass = "AC",
        hitPoints = "HP",
        challengeRating = "Challenge Rating"
    )
)

private fun sampleNpc(
    nickname: String = "The Swift",
    personalityTraits: List<String> = listOf("Brave", "Curious"),
    items: List<String> = emptyList()
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
    notes = "",
    strength = null,
    dexterity = null,
    constitution = null,
    intelligence = null,
    wisdom = null,
    charisma = null,
    armorClass = null,
    hitPoints = null,
    challengeRating = null,
    campaign = null,
    items = items
)

@OptIn(ExperimentalTestApi::class)
@RobolectricTest(sdk = [34], application = Application::class)
class NpcShareCardTest : StringSpec({

    "renders the npc key attributes and watermark" {
        runAndroidComposeUiTest<ComponentActivity> {
            setContent {
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
            onNodeWithText("Aria Nightsong").assertExists()
            onNodeWithText("The Swift", substring = true).assertExists()
            onNodeWithText("Human · Adult · Female").assertExists()
            onNodeWithText("Blacksmith").assertExists()
            onNodeWithText("Neutral Good").assertExists()
            onNodeWithText("Protect the forest").assertExists()
            onNodeWithText("Brave").assertExists()
            onNodeWithText("Curious").assertExists()
            onNodeWithText("Common").assertExists()
            onNodeWithText("Created with RPG NPC Generator").assertExists()
        }
    }

    "renders the combat stat block with ability modifiers when the npc has stats" {
        runAndroidComposeUiTest<ComponentActivity> {
            setContent {
                MaterialTheme {
                    NpcShareCard(
                        npc = sampleNpc().copy(strength = 14, armorClass = 15, challengeRating = "1/4"),
                        portrait = null,
                        footer = "Created with RPG NPC Generator",
                        labels = labels
                    )
                }
            }

            onNodeWithText("14 (+2)").assertExists()
            onNodeWithText("AC", substring = true).assertExists()
            onNodeWithText("1/4", substring = true).assertExists()
        }
    }

    "omits the combat stat block for a statless npc" {
        runAndroidComposeUiTest<ComponentActivity> {
            setContent {
                MaterialTheme {
                    NpcShareCard(
                        npc = sampleNpc(),
                        portrait = null,
                        footer = "Created with RPG NPC Generator",
                        labels = labels
                    )
                }
            }

            // Section titles render uppercased, so the block's presence would show as "COMBAT STATS".
            onNodeWithText("COMBAT STATS").assertDoesNotExist()
        }
    }

    "renders the items section as chips when the npc has items" {
        runAndroidComposeUiTest<ComponentActivity> {
            setContent {
                MaterialTheme {
                    NpcShareCard(
                        npc = sampleNpc(items = listOf("A set of smith's tools", "A worn dagger")),
                        portrait = null,
                        footer = "Created with RPG NPC Generator",
                        labels = labels
                    )
                }
            }

            onNodeWithText("ITEMS").assertExists()
            onNodeWithText("A set of smith's tools").assertExists()
            onNodeWithText("A worn dagger").assertExists()
        }
    }

    "omits the items section when the npc has no items" {
        runAndroidComposeUiTest<ComponentActivity> {
            setContent {
                MaterialTheme {
                    NpcShareCard(
                        npc = sampleNpc(items = emptyList()),
                        portrait = null,
                        footer = "Created with RPG NPC Generator",
                        labels = labels
                    )
                }
            }

            onNodeWithText("ITEMS").assertDoesNotExist()
        }
    }

    "omits the nickname line when the npc has no nickname" {
        runAndroidComposeUiTest<ComponentActivity> {
            setContent {
                MaterialTheme {
                    NpcShareCard(
                        npc = sampleNpc(nickname = ""),
                        portrait = null,
                        footer = "Created with RPG NPC Generator",
                        labels = labels
                    )
                }
            }

            onNodeWithText("Aria Nightsong").assertExists()
            onNodeWithText("“", substring = true).assertDoesNotExist()
        }
    }
})
