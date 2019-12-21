package me.kerooker.rpgnpcgenerator.legacy.repository

import io.kotest.IsolationMode
import io.kotest.TestCase
import io.kotest.TestResult
import io.kotest.android.withSharedPreferences
import io.kotest.experimental.robolectric.RobolectricTest
import io.kotest.extensions.TestListener
import io.kotest.shouldBe
import io.kotest.specs.FunSpec
import org.koin.core.KoinComponent
import org.koin.core.context.stopKoin
import java.util.UUID

@RobolectricTest
class LegacyNpcRepositoryTest : FunSpec(), KoinComponent {
    
    private val target
    get() = LegacyNpcRepository()

    init {
        test("When shared preferences is empty should do nothing") {
            target.loadLegacyNpcs() shouldBe emptyList()
        }

        test("An NPCs information should be loaded correctly") {
            val randomUUID = UUID.randomUUID().toString()

            withSharedPreferences("saved_npcs", randomUUID, npcJson) {
                target.loadLegacyNpcs() shouldBe listOf(legacyNpc)
            }
        }

        test("If more than one NPC is present, a list should be returned") {
            val randomUUID = UUID.randomUUID().toString()
            val randomUUID2 = UUID.randomUUID().toString()

            withSharedPreferences("saved_npcs", randomUUID, npcJson) {
                withSharedPreferences("saved_npcs", randomUUID2, npcJson) {
                    target.loadLegacyNpcs() shouldBe listOf(legacyNpc, legacyNpc)
                }
            }
        }
    }

    override fun isolationMode() = IsolationMode.InstancePerTest

    override fun listeners() = listOf<TestListener>(object: TestListener {
        override fun afterTest(testCase: TestCase, result: TestResult) {
            stopKoin()
        }
    })
}

private val legacyNpc = LegacyNpc(
    "Svarog Drevion, The authentic",
    "Child",
    "Elf",
    "Eladrin",
    "Female",
    "Chaotic Evil",
    "Homosexual",
    "Sorcerer's Apprentice",
    "Pride",
    listOf("Subtle", "Artificial", "Extreme", "Graceless"),
    "Fear of amnesia",
    listOf("Common", "Elven", "Sylvan")
)

// NPC Json extracted from the legacy app.
private val npcJson = "{&quot;imageBits&quot;:&quot;j&quot;,&quot;information&quot;:[{&quot;type&quot;:&quot;me.kerooker.characterinformation.Name&quot;,&quot;data&quot;:{&quot;language&quot;:&quot;en&quot;,&quot;name&quot;:&quot;Svarog Drevion, The authentic&quot;}},{&quot;type&quot;:&quot;me.kerooker.characterinformation.Age&quot;,&quot;data&quot;:{&quot;age&quot;:&quot;CHILD&quot;}},{&quot;type&quot;:&quot;me.kerooker.characterinformation.Race&quot;,&quot;data&quot;:{&quot;race&quot;:&quot;Elf&quot;,&quot;subrace&quot;:&quot;Eladrin&quot;}},{&quot;type&quot;:&quot;me.kerooker.characterinformation.Gender&quot;,&quot;data&quot;:{&quot;gender&quot;:&quot;FEMALE&quot;}},{&quot;type&quot;:&quot;me.kerooker.characterinformation.Alignment&quot;,&quot;data&quot;:{&quot;align&quot;:&quot;CHAOTIC_EVIL&quot;}},{&quot;type&quot;:&quot;me.kerooker.characterinformation.Sexuality&quot;,&quot;data&quot;:{&quot;sexuality&quot;:&quot;HOMOSEXUAL&quot;}},{&quot;type&quot;:&quot;me.kerooker.characterinformation.Profession&quot;,&quot;data&quot;:{&quot;age&quot;:&quot;CHILD&quot;,&quot;profession&quot;:&quot;Sorcerer\\u0027s Apprentice&quot;}},{&quot;type&quot;:&quot;me.kerooker.characterinformation.Motivation&quot;,&quot;data&quot;:{&quot;motivation&quot;:&quot;Pride&quot;}},{&quot;type&quot;:&quot;me.kerooker.characterinformation.PersonalityTraits&quot;,&quot;data&quot;:{&quot;traits&quot;:[&quot;Subtle&quot;,&quot;Artificial&quot;,&quot;Extreme&quot;,&quot;Graceless&quot;]}},{&quot;type&quot;:&quot;me.kerooker.characterinformation.Phobia&quot;,&quot;data&quot;:{&quot;phobia&quot;:&quot;Fear of amnesia&quot;}},{&quot;type&quot;:&quot;me.kerooker.characterinformation.Language&quot;,&quot;data&quot;:{&quot;race&quot;:{&quot;race&quot;:&quot;Elf&quot;,&quot;subrace&quot;:&quot;Eladrin&quot;},&quot;spoken&quot;:[&quot;COMMON&quot;,&quot;ELVEN&quot;,&quot;SYLVAN&quot;]}}],&quot;uuid&quot;:&quot;2d5a1789-25a1-44ea-89d3-7979ed450969&quot;}"