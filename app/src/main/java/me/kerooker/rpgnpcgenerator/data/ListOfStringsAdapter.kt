package me.kerooker.rpgnpcgenerator.data

import app.cash.sqldelight.ColumnAdapter
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

/**
 * Persists a [List] of arbitrary user strings as a JSON array. Arbitrary text (commas, quotes,
 * emoji) makes a delimiter scheme unsafe, so we encode the list as JSON.
 */
object ListOfStringsAdapter : ColumnAdapter<List<String>, String> {

    private val serializer = ListSerializer(String.serializer())

    override fun decode(databaseValue: String): List<String> =
        if (databaseValue.isEmpty()) {
            emptyList()
        } else {
            Json.decodeFromString(serializer, databaseValue)
        }

    override fun encode(value: List<String>): String =
        Json.encodeToString(serializer, value)
}
