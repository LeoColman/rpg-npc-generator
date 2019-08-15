package me.kerooker.rpgnpcgenerator.repository.model.npc

import android.content.Context
import androidx.annotation.RawRes
import me.kerooker.rpgnpcgenerator.R
import org.koin.dsl.module

val fileGeneratorModule = module {
    single { NameGenerator(get()) }
    single { NicknameGenerator(get()) }
    single { ProfessionGenerator(get()) }
    single { ChildProfessionGenerator(get()) }
    single { MotivationGenerator(get()) }
    single { PersonalityTraitGenerator(get()) }
}

abstract class FileGenerator(
    @RawRes val fileResource: Int,
    context: Context
)  {

    private val fileLines by lazy { linesFromRaw(fileResource, context) }

    fun random(): String = fileLines.random()
}

class NameGenerator(context: Context) : FileGenerator(R.raw.npc_names, context)

class NicknameGenerator(context: Context) : FileGenerator(R.raw.npc_nicknames, context)

class ProfessionGenerator(context: Context) : FileGenerator(R.raw.npc_professions, context)

class ChildProfessionGenerator(context: Context) : FileGenerator(R.raw.npc_child_professions, context)

class MotivationGenerator(context: Context) : FileGenerator(R.raw.npc_motivations, context)

class PersonalityTraitGenerator(context: Context) : FileGenerator(R.raw.npc_personality_trait, context)


fun linesFromRaw(@RawRes rawResource: Int, context: Context) =
    context.resources.openRawResource(rawResource).bufferedReader().readLines()
