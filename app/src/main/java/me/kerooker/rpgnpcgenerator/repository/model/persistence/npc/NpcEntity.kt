package me.kerooker.rpgnpcgenerator.repository.model.persistence.npc

import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.converter.PropertyConverter
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import kotlinx.serialization.serializer
import kotlinx.serialization.stringify

@Entity data class NpcEntity(
    val fullName: String,
    
    val nickname: String,
    
    val gender: String,
    
    val sexuality: String,
    
    val race: String,
    
    val age: String,
    
    val profession: String,
    
    val motivation: String,
    
    val alignment: String,
    
    @Convert(converter = StringListConverter::class, dbType = String::class)
    val personalityTraits: List<String>,
    
    @Convert(converter = StringListConverter::class, dbType = String::class)
    val languages: List<String>,
    
    val imagePath: String?,
    
    val notes: String = "",
    
    @Id var id: Long = 0
)

private class StringListConverter : PropertyConverter<List<String>, String> {

    private val json = Json(JsonConfiguration.Stable)

    @UseExperimental(ImplicitReflectionSerializer::class)
    override fun convertToDatabaseValue(entityProperty: List<String>): String {
        return json.stringify(entityProperty)
    }
    
    @UseExperimental(ImplicitReflectionSerializer::class)
    override fun convertToEntityProperty(databaseValue: String): List<String> {
        return json.parse(String.serializer().list, databaseValue)
    }
}
