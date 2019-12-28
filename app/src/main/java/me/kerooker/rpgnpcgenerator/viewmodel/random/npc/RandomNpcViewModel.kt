package me.kerooker.rpgnpcgenerator.viewmodel.random.npc

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.kerooker.rpgnpcgenerator.repository.model.persistence.npc.NpcEntity
import me.kerooker.rpgnpcgenerator.repository.model.persistence.npc.NpcRepository
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.Age
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.CompleteNpcGenerator
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.GeneratedNpc
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.GeneratedNpcData
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.Language
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.NpcDataGenerator
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.TemporaryRandomNpcRepository

@Suppress("TooManyFunctions")
class RandomNpcViewModel(
    private val context: Context,
    private val completeNpcGenerator: CompleteNpcGenerator,
    private val npcDataGenerator: NpcDataGenerator,
    private val temporaryRandomNpcRepository: TemporaryRandomNpcRepository,
    private val npcRepository: NpcRepository
) : ViewModel() {
    
    
    val data: LiveData<GeneratedNpcData> by lazy {
        if(temporaryRandomNpcRepository.generatedNpcData.value == null) {
            temporaryRandomNpcRepository.setNpc(completeNpcGenerator.generate().toNpcData())
        }
        temporaryRandomNpcRepository.generatedNpcData
    }
    
    fun randomizeName() = setName(npcDataGenerator.generateFullName())
    
    fun setName(name: String) = temporaryRandomNpcRepository.setFullName(name)
    
    fun randomizeNickname() {
        val nickname = npcDataGenerator.generateNickname()
        setNickname(nickname)
    }
    
    fun setNickname(nickname: String) = temporaryRandomNpcRepository.setNickname(nickname)
    
    
    fun randomizeRace() {
        val race = npcDataGenerator.generateRace()
        temporaryRandomNpcRepository.setRace(context.getString(race.nameResource))
    }
    
    fun setRace(race: String) = temporaryRandomNpcRepository.setRace(race)
    
    fun randomizeAge() {
        val age = npcDataGenerator.generateAge()
        if(age.isNotSameGroupAsCurrentAge()) {
            randomizeProfession()   // It's possible that the generated age has an incompatible profession
        }
        setAge(context.getString(age.nameResource))
    }
    
    private fun Age.isNotSameGroupAsCurrentAge(): Boolean {
        return when {
            currentAgeIsChild() && this != Age.Child  -> true
            !currentAgeIsChild() && this == Age.Child -> true
            else                                      -> false
        }
    }
    
    fun setAge(age: String) = temporaryRandomNpcRepository.setAge(age)
    
    fun randomizeGender() {
        val gender = npcDataGenerator.generateGender()
        setGender(context.getString(gender.nameResource))
    }
    
    fun setGender(gender: String) = temporaryRandomNpcRepository.setGender(gender)
    
    fun randomizeProfession() {
        val profession = if(currentAgeIsChild()) {
            npcDataGenerator.generateProfession(Age.Child)
        } else {
            npcDataGenerator.generateProfession(Age.Adult)
        }
        setProfession(profession)
    }
    
    private fun currentAgeIsChild(): Boolean {
        val childResource = context.getString(Age.Child.nameResource)
        return data.value?.age.equals(childResource, true)
    }
    
    fun setProfession(profession: String) = temporaryRandomNpcRepository.setProfession(profession)
    
    fun randomizeSexuality() {
        val sexuality = npcDataGenerator.generateSexuality()
        setSexuality(context.getString(sexuality.nameResource))
    }
    
    fun setSexuality(sexuality: String) = temporaryRandomNpcRepository.setSexuality(sexuality)
    
    fun randomizeAlignment() {
        val alignment = npcDataGenerator.generateAlignment()
        setAlignment(context.getString(alignment.nameResource))
    }
    
    fun setAlignment(alignment: String) = temporaryRandomNpcRepository.setAlignment(alignment)
    
    fun randomizeMotivation() {
        val motivation = npcDataGenerator.generateMotivation()
        setMotivation(motivation)
    }
    
    fun setMotivation(motivation: String) = temporaryRandomNpcRepository.setMotivation(motivation)
    
    fun randomizeLanguage(index: Int) {
        val languages = Language.values().map { context.getString(it.nameResource) }
        val newRandomLanguages = languages.filter { it !in (data.value?.languages ?: emptyList<String>()) }
        
        val randomLanguage = if (newRandomLanguages.isEmpty()) languages.first() else newRandomLanguages.random()
        
        setLanguage(index, randomLanguage)
    }
    
    fun setLanguage(index: Int, language: String) {
        temporaryRandomNpcRepository.setLanguage(index, language)
    }
    
    fun removeLanguage(index: Int) = temporaryRandomNpcRepository.removeLanguage(index)
    
    fun randomizePersonality(index: Int) {
        val personality = npcDataGenerator.generatePersonalityTrait()
        setPersonality(index, personality)
    }
    
    fun setPersonality(index: Int, personality: String) =
        temporaryRandomNpcRepository.setPersonality(index, personality)
    
    fun removePersonality(index: Int) = temporaryRandomNpcRepository.removePersonality(index)
    
    fun randomizeAll() {
        val randomNpc = completeNpcGenerator.generate()
        temporaryRandomNpcRepository.setNpc(randomNpc.toNpcData())
    }
    
    private fun GeneratedNpc.toNpcData(): GeneratedNpcData {
        return GeneratedNpcData(
            name,
            nickname,
            context.getString(gender.nameResource),
            context.getString(sexuality.nameResource),
            context.getString(race.nameResource),
            context.getString(age.nameResource),
            profession,
            motivation,
            context.getString(alignment.nameResource),
            personalityTraits.toMutableList(),
            languages.map { context.getString(it.nameResource) }.toMutableList()
        )
    }
    
    fun saveCurrentNpc() {
        val npcToPersist = data.value!!.toEntity()
        viewModelScope.launch(Dispatchers.IO) {
            npcRepository.put(npcToPersist)
        }
    }
    
    private fun GeneratedNpcData.toEntity() =
        NpcEntity(
            this.name,
            this.nickname,
            this.gender,
            this.sexuality,
            this.race,
            this.age,
            this.profession,
            this.motivation,
            this.alignment,
            this.personalityTraits,
            this.languages,
            imagePath = null
        )
}
