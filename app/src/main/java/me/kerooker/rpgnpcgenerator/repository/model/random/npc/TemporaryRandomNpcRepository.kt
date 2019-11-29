package me.kerooker.rpgnpcgenerator.repository.model.random.npc

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

@Suppress("TooManyFunctions")
class TemporaryRandomNpcRepository {
    
    private val _generatedNpcData = MutableLiveData<GeneratedNpcData>()
    
    val generatedNpcData: LiveData<GeneratedNpcData>
        get() = _generatedNpcData
    
    private val current
        get() = generatedNpcData.value ?: throw IllegalStateException("Repository was not started before usage")
    
    fun setNpc(npc: GeneratedNpcData) {
        _generatedNpcData.value = npc
    }
    
    fun setFullName(name: String) = updating { it.name = name }
    
    fun setNickname(nickname: String) = updating { it.nickname = nickname }
    
    fun setRace(race: String) = updating { it.race = race }
    
    fun setGender(gender: String) = updating { it.gender = gender }
    
    fun setAge(age: String) = updating { it.age = age }
    
    fun setProfession(profession: String) = updating { it.profession = profession }
    
    fun setSexuality(sexuality: String) = updating { it.sexuality = sexuality }
    
    fun setAlignment(alignment: String) = updating { it.alignment = alignment }
    
    fun setMotivation(motivation: String) = updating { it.motivation = motivation }
    
    fun setLanguage(index: Int, language: String) = updating {
        if (index in it.languages.indices) {
            it.languages[index] = language
        } else {
            it.languages.add(language)
        }
    }
    
    fun removeLanguage(index: Int) = updating { it.languages.removeAt(index) }
    
    fun setPersonality(index: Int, personality: String) = updating {
        if(index in it.personalityTraits.indices) {
            it.personalityTraits[index] = personality
        } else {
            it.personalityTraits.add(personality)
        }
    }
    
    fun removePersonality(index: Int) = updating { it.personalityTraits.removeAt(index) }
    
    private inline fun updating(block: (GeneratedNpcData) -> Unit) {
        block(current)
        _generatedNpcData.value = _generatedNpcData.value
    }
}
