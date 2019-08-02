package me.kerooker.rpgnpcgenerator.legacy.repository

import android.content.Context
import com.beust.klaxon.JsonArray
import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import org.koin.core.KoinComponent
import org.koin.core.inject

const val SAVED_NPCS_SHARED_PREFERENCES_NAME = "saved_npcs"

@Suppress("UNCHECKED_CAST")
class LegacyNpcRepository : KoinComponent {

    private val context by inject<Context>()

    fun loadLegacyNpcs(): List<LegacyNpc> {
        val npcJsons = getNpcJsons()

        return npcJsons.map {
            val information = it.getInformationArray()
            LegacyNpc(
                information.name,
                information.age,
                information.race,
                information.subRace,
                information.gender,
                information.alignment,
                information.sexuality,
                information.profession,
                information.motivation,
                information.personalityTraits,
                information.fear,
                information.languages
            )
        }
    }

    private fun getNpcJsons(): List<JsonObject> {
        val jsonString = (getSavedNpcsSharedPreferences().all.values as Collection<String>)

        return jsonString.map {
            Parser.default().parse(StringBuilder(it.replaceQuote())) as JsonObject
        }
    }

    private fun getSavedNpcsSharedPreferences() =
        context.getSharedPreferences(SAVED_NPCS_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    private fun String.replaceQuote(): String = replace("&quot;", "\"")

    private fun JsonObject.getInformationArray() = array<JsonObject>("information")!!

    private val JsonArray<JsonObject>.name: String
        get() = stringOfType("Name", "name")!!

    private val JsonArray<JsonObject>.age: String
        get() = stringOfType("Age", "age")!!.withUnderscoresReplaced().toLowerCase().capitalizeWords()

    private val JsonArray<JsonObject>.race: String
        get() = stringOfType("Race", "race")!!

    private val JsonArray<JsonObject>.subRace: String?
        get() = stringOfType("Race", "subrace")

    private val JsonArray<JsonObject>.gender: String
        get() = stringOfType("Gender", "gender")!!.toLowerCase().capitalizeWords()

    private val JsonArray<JsonObject>.alignment: String
        get() = stringOfType("Alignment", "align")!!.withUnderscoresReplaced().toLowerCase().capitalizeWords()

    private val JsonArray<JsonObject>.sexuality: String
        get() = stringOfType("Sexuality", "sexuality")!!.toLowerCase().capitalizeWords()

    private val JsonArray<JsonObject>.profession: String
        get() = stringOfType("Profession", "profession")!!

    private val JsonArray<JsonObject>.motivation: String
        get() = stringOfType("Motivation", "motivation")!!

    private val JsonArray<JsonObject>.personalityTraits: List<String>
        get() = stringListOfType("PersonalityTraits", "traits")!!.value

    private val JsonArray<JsonObject>.fear: String
        get() = stringOfType("Phobia", "phobia")!!

    private val JsonArray<JsonObject>.languages: List<String>
        get() = stringListOfType("Language", "spoken")!!.map { it.toLowerCase().capitalizeWords() }

    private fun JsonArray<JsonObject>.stringListOfType(type: String, string: String) =
        firstOfType(type).node("data").array<String>(string)

    private fun JsonArray<JsonObject>.stringOfType(type: String, string: String) =
        firstOfType(type).node("data").string(string)

    private fun JsonArray<JsonObject>.firstOfType(type: String) =
        first { it["type"] == "me.kerooker.characterinformation.$type" }

    private fun JsonObject.node(key: String) = get(key) as JsonObject

    private fun String.withUnderscoresReplaced() = replace("_", " ")

    private fun String.capitalizeWords() = split(" ").joinToString(separator = " ") { it.capitalize() }
}

data class LegacyNpc(
    val name: String,
    val age: String,
    val race: String,
    val subRace: String?,
    val gender: String,
    val alignment: String,
    val sexuality: String,
    val profession: String,
    val motivation: String,
    val personalityTraits: List<String>,
    val fear: String,
    val languages: List<String>
)
