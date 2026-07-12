package me.kerooker.rpgnpcgenerator.repository.model.random.npc

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds the NPC currently being rolled on the generator screen as an observable [StateFlow], so it
 * survives navigation between tabs (the instance is a Koin singleton).
 */
@Suppress("TooManyFunctions")
class TemporaryRandomNpcRepository {

    private val _generatedNpcData = MutableStateFlow<GeneratedNpcData?>(null)
    val generatedNpcData: StateFlow<GeneratedNpcData?> = _generatedNpcData.asStateFlow()

    private val current
        get() = _generatedNpcData.value ?: error("Repository was not started before usage")

    fun setNpc(npc: GeneratedNpcData) {
        _generatedNpcData.value = npc
    }

    fun setFullName(name: String) = update { it.copy(name = name) }

    fun setNickname(nickname: String) = update { it.copy(nickname = nickname) }

    fun setRace(race: String) = update { it.copy(race = race) }

    fun setGender(gender: String) = update { it.copy(gender = gender) }

    fun setAge(age: String) = update { it.copy(age = age) }

    fun setProfession(profession: String) = update { it.copy(profession = profession) }

    fun setSexuality(sexuality: String) = update { it.copy(sexuality = sexuality) }

    fun setAlignment(alignment: String) = update { it.copy(alignment = alignment) }

    fun setMotivation(motivation: String) = update { it.copy(motivation = motivation) }

    fun setLanguage(index: Int, language: String) = update { data ->
        val languages = data.languages.toMutableList()
        if (index in languages.indices) languages[index] = language else languages.add(language)
        data.copy(languages = languages)
    }

    fun removeLanguage(index: Int) = update { data ->
        data.copy(languages = data.languages.toMutableList().apply { removeAt(index) })
    }

    fun setPersonality(index: Int, personality: String) = update { data ->
        val traits = data.personalityTraits.toMutableList()
        if (index in traits.indices) traits[index] = personality else traits.add(personality)
        data.copy(personalityTraits = traits)
    }

    fun removePersonality(index: Int) = update { data ->
        data.copy(personalityTraits = data.personalityTraits.toMutableList().apply { removeAt(index) })
    }

    fun setPersonalities(personalities: List<String>) = update { it.copy(personalityTraits = personalities) }

    fun setCombat(combat: CombatStats) = update { it.copy(combat = combat) }

    private inline fun update(block: (GeneratedNpcData) -> GeneratedNpcData) {
        _generatedNpcData.value = block(current)
    }
}
