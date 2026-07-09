package me.kerooker.rpgnpcgenerator.repository.model.random.npc

import android.content.Context
import androidx.annotation.RawRes

abstract class FileGenerator(
    private val lines: List<String>
) {
    fun random(): String = lines.random()
}

class NameGenerator(lines: List<String>) : FileGenerator(lines)

class NicknameGenerator(lines: List<String>) : FileGenerator(lines)

class CommonProfessionGenerator(lines: List<String>) : FileGenerator(lines)

class ChildProfessionGenerator(lines: List<String>) : FileGenerator(lines)

class MotivationGenerator(lines: List<String>) : FileGenerator(lines)

class PersonalityTraitGenerator(lines: List<String>) : FileGenerator(lines)

fun linesFromRaw(@RawRes rawResource: Int, context: Context): List<String> =
    context.resources.openRawResource(rawResource).bufferedReader().readLines()

class ProfessionGenerator(
    private val childProfessionGenerator: ChildProfessionGenerator,
    private val commonProfessionGenerator: CommonProfessionGenerator
) {
    fun random(age: Age): String {
        return when (age) {
            Age.Child -> childProfessionGenerator.random()
            else -> commonProfessionGenerator.random()
        }
    }
}
