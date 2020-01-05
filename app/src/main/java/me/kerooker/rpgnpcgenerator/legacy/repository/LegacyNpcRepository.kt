package me.kerooker.rpgnpcgenerator.legacy.repository

import android.content.Context
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonObject
import org.koin.core.KoinComponent
import org.koin.core.inject

const val SAVED_NPCS_SHARED_PREFERENCES_NAME = "saved_npcs"

@Suppress("UNCHECKED_CAST")
class LegacyNpcRepository : KoinComponent {

    private val json = Json(JsonConfiguration.Stable)
    private val context by inject<Context>()

    fun loadLegacyNpcs(): List<LegacyNpc> {
        val npcJsons = getNpcJsons()
    
        return npcJsons.mapNotNull {
            try {
                val information = it.getInformationArray()
                LegacyNpc(
                    tryOrEmpty { information.name },
                    tryOrEmpty { information.age },
                    tryOrEmpty { information.race },
                    tryOrEmptyN { information.subRace },
                    tryOrEmpty { information.gender },
                    tryOrEmpty { information.alignment },
                    tryOrEmpty { information.sexuality },
                    tryOrEmpty { information.profession },
                    tryOrEmpty { information.motivation },
                    tryOrEmptyL { information.personalityTraits },
                    tryOrEmpty { information.fear },
                    tryOrEmptyL { information.languages }
                )
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun getNpcJsons(): List<JsonObject> {
        val jsonString = (getSavedNpcsSharedPreferences().all.values as Collection<String>)

        return jsonString.map {
            json.parseJson(it.replaceQuote()) as JsonObject
        }
    }

    private fun getSavedNpcsSharedPreferences() =
        context.getSharedPreferences(SAVED_NPCS_SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    private fun String.replaceQuote(): String = replace("&quot;", "\"")

    private fun JsonObject.getInformationArray() = getArray("information")

    private val JsonArray.name: String
        get() = stringOfType("Name", "name")

    private val JsonArray.age: String
        get() = stringOfType("Age", "age").withUnderscoresReplaced().toLowerCase().capitalizeWords()

    private val JsonArray.race: String
        get() = stringOfType("Race", "race")

    private val JsonArray.subRace: String?
        get() = stringOfType("Race", "subrace")

    private val JsonArray.gender: String
        get() = stringOfType("Gender", "gender").toLowerCase().capitalizeWords()

    private val JsonArray.alignment: String
        get() = stringOfType("Alignment", "align").withUnderscoresReplaced().toLowerCase().capitalizeWords()

    private val JsonArray.sexuality: String
        get() = stringOfType("Sexuality", "sexuality").toLowerCase().capitalizeWords()

    private val JsonArray.profession: String
        get() = stringOfType("Profession", "profession")

    private val JsonArray.motivation: String
        get() = stringOfType("Motivation", "motivation")

    private val JsonArray.personalityTraits: List<String>
        get() = stringListOfType("PersonalityTraits", "traits")

    private val JsonArray.fear: String
        get() = stringOfType("Phobia", "phobia")

    private val JsonArray.languages: List<String>
        get() = stringListOfType("Language", "spoken").map { it.toLowerCase().capitalizeWords() }

    private fun JsonArray.stringListOfType(type: String, string: String) =
        firstOfType(type).jsonObject.node("data")[string]!!.jsonArray.map { it.primitive.content }

    private fun JsonArray.stringOfType(type: String, string: String) =
        firstOfType(type).jsonObject.node("data")[string]!!.primitive.content

    private fun JsonArray.firstOfType(type: String) =
        first { it.jsonObject.getPrimitive("type").content == "me.kerooker.characterinformation.$type" }

    private fun JsonObject.node(key: String) = get(key) as JsonObject

    private fun String.withUnderscoresReplaced() = replace("_", " ")
    
    private fun String.capitalizeWords() = split(" ").joinToString(separator = " ") { it.capitalize() }
    
    private fun tryOrEmpty(block: () -> String) = try {
        block()
    } catch (_: Exception) {
        ""
    }
    
    private fun tryOrEmptyN(block: () -> String?) = try {
        block()
    } catch (_: Exception) {
        null
    }
    
    private fun tryOrEmptyL(block: () -> List<String>) = try {
        block()
    } catch (_: Exception) {
        emptyList<String>()
    }
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
