package me.kerooker.rpgnpcgenerator.repository.model.persistence

import com.beust.klaxon.Klaxon
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.converter.PropertyConverter

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

    @Id var id: Long = 0
)

private class StringListConverter : PropertyConverter<List<String>, String> {


    override fun convertToDatabaseValue(entityProperty: List<String>?): String {
        return Klaxon().toJsonString(entityProperty)
    }

    override fun convertToEntityProperty(databaseValue: String): List<String> {
        return Klaxon().parseArray(databaseValue) ?: emptyList()
    }
}
