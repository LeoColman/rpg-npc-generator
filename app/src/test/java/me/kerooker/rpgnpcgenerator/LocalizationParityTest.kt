package me.kerooker.rpgnpcgenerator

import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import java.io.File

/**
 * Guards the completeness of every translated locale. A half-translated resource silently falls back
 * to English per-string (XML) or breaks the one-entry-per-line contract (raw), so both are enforced:
 *
 *  - Every localized XML file must expose exactly the same `<string>` key set as the default (en)
 *    locale — checked for both `pt` and `es` so either drifting is caught.
 *  - Every Spanish raw generator file must have line-count parity with its English source, carry no
 *    blank lines, and hold no duplicate entries.
 *
 * Portuguese raw files are intentionally a curated (shorter) subset, so raw parity is asserted for
 * Spanish only. Tests run with the module dir (`app/`) as the working directory — see FileGeneratorsTest.
 */
class LocalizationParityTest : FreeSpec({

    "every localized XML file exposes the same string keys as the default (en) locale" - {
        translatedLocales.forEach { locale ->
            localizedXmlFiles.forEach { file ->
                "values-$locale/$file matches values/$file" {
                    stringKeys("src/main/res/values-$locale/$file") shouldBe
                        stringKeys("src/main/res/values/$file")
                }
            }
        }
    }

    "every Spanish raw generator file mirrors its English source" - {
        localizedRawFiles.forEach { file ->
            val english = File("src/main/res/raw/$file").readLines()
            val spanish = File("src/main/res/raw-es/$file").readLines()

            "raw-es/$file has line-count parity with raw/$file" {
                spanish.size shouldBe english.size
            }

            "raw-es/$file has no blank lines" {
                spanish.any { it.isBlank() } shouldBe false
            }

            "raw-es/$file has no duplicate entries" {
                spanish.distinct().size shouldBe spanish.size
            }
        }
    }
}) {
    private companion object {
        val translatedLocales = listOf("pt", "es")

        val localizedXmlFiles = listOf(
            "strings.xml",
            "npc_enums.xml",
            "settings.xml",
            "mynpcs.xml",
            "individual_npc.xml",
            "random_npc.xml"
        )

        val localizedRawFiles = listOf(
            "npc_child_professions.txt",
            "npc_motivations.txt",
            "npc_nicknames.txt",
            "npc_personality_trait.txt",
            "npc_professions.txt"
        )

        // Only <string> elements — deliberately ignores <declare-styleable>/<attr> so a styleable
        // declared in one locale's file (values-pt/random_npc.xml) never counts as a translatable key.
        val stringKeyPattern = Regex("""<string\s+name="([^"]+)"""")

        fun stringKeys(path: String): Set<String> =
            stringKeyPattern.findAll(File(path).readText()).map { it.groupValues[1] }.toSet()
    }
}
