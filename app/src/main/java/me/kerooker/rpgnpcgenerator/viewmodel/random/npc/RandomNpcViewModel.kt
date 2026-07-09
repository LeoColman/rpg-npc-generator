package me.kerooker.rpgnpcgenerator.viewmodel.random.npc

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.kerooker.rpgnpcgenerator.data.Npc
import me.kerooker.rpgnpcgenerator.data.NpcRepository
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

    init {
        if (temporaryRandomNpcRepository.generatedNpcData.value == null) {
            temporaryRandomNpcRepository.setNpc(completeNpcGenerator.generate().toNpcData())
        }
    }

    val data: StateFlow<GeneratedNpcData> =
        temporaryRandomNpcRepository.generatedNpcData
            .filterNotNull()
            .stateIn(
                viewModelScope,
                SharingStarted.Eagerly,
                requireNotNull(temporaryRandomNpcRepository.generatedNpcData.value)
            )

    fun randomizeName() = setName(npcDataGenerator.generateFullName())

    fun setName(name: String) = temporaryRandomNpcRepository.setFullName(name)

    fun randomizeNickname() = setNickname(npcDataGenerator.generateNickname())

    fun setNickname(nickname: String) = temporaryRandomNpcRepository.setNickname(nickname)

    fun randomizeRace() {
        val race = npcDataGenerator.generateRace()
        temporaryRandomNpcRepository.setRace(context.getString(race.nameResource))
    }

    fun setRace(race: String) = temporaryRandomNpcRepository.setRace(race)

    fun randomizeAge() {
        val age = npcDataGenerator.generateAge()
        if (age.isNotSameGroupAsCurrentAge()) {
            randomizeProfession() // A newly generated age may have an incompatible profession.
        }
        setAge(context.getString(age.nameResource))
    }

    private fun Age.isNotSameGroupAsCurrentAge(): Boolean {
        return when {
            currentAgeIsChild() && this != Age.Child -> true
            !currentAgeIsChild() && this == Age.Child -> true
            else -> false
        }
    }

    fun setAge(age: String) = temporaryRandomNpcRepository.setAge(age)

    fun randomizeGender() {
        val gender = npcDataGenerator.generateGender()
        setGender(context.getString(gender.nameResource))
    }

    fun setGender(gender: String) = temporaryRandomNpcRepository.setGender(gender)

    fun randomizeProfession() {
        val profession = if (currentAgeIsChild()) {
            npcDataGenerator.generateProfession(Age.Child)
        } else {
            npcDataGenerator.generateProfession(Age.Adult)
        }
        setProfession(profession)
    }

    private fun currentAgeIsChild(): Boolean {
        val childResource = context.getString(Age.Child.nameResource)
        return data.value.age.equals(childResource, true)
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

    fun randomizeMotivation() = setMotivation(npcDataGenerator.generateMotivation())

    fun setMotivation(motivation: String) = temporaryRandomNpcRepository.setMotivation(motivation)

    fun randomizeLanguage(index: Int) {
        val languages = Language.values().map { context.getString(it.nameResource) }
        val newRandomLanguages = languages.filter { it !in data.value.languages }
        val randomLanguage = if (newRandomLanguages.isEmpty()) languages.first() else newRandomLanguages.random()
        setLanguage(index, randomLanguage)
    }

    fun setLanguage(index: Int, language: String) = temporaryRandomNpcRepository.setLanguage(index, language)

    fun removeLanguage(index: Int) = temporaryRandomNpcRepository.removeLanguage(index)

    fun randomizePersonality(index: Int) =
        setPersonality(index, npcDataGenerator.generatePersonalityTrait())

    fun setPersonality(index: Int, personality: String) =
        temporaryRandomNpcRepository.setPersonality(index, personality)

    fun removePersonality(index: Int) = temporaryRandomNpcRepository.removePersonality(index)

    fun randomizeAllPersonalities() {
        val count = data.value.personalityTraits.size.coerceAtLeast(1)
        val fresh = List(count) { npcDataGenerator.generatePersonalityTrait() }
        temporaryRandomNpcRepository.setPersonalities(fresh)
    }

    fun randomizeAll() {
        temporaryRandomNpcRepository.setNpc(completeNpcGenerator.generate().toNpcData())
    }

    fun saveCurrentNpc() {
        val npc = data.value.toNpc()
        viewModelScope.launch(Dispatchers.IO) {
            npcRepository.insert(npc)
        }
    }

    private fun GeneratedNpc.toNpcData(): GeneratedNpcData {
        return GeneratedNpcData(
            name = name,
            nickname = nickname,
            gender = context.getString(gender.nameResource),
            sexuality = context.getString(sexuality.nameResource),
            race = context.getString(race.nameResource),
            age = context.getString(age.nameResource),
            profession = profession,
            motivation = motivation,
            alignment = context.getString(alignment.nameResource),
            personalityTraits = personalityTraits,
            languages = languages.map { context.getString(it.nameResource) }
        )
    }

    private fun GeneratedNpcData.toNpc() = Npc(
        id = 0,
        fullName = name,
        nickname = nickname,
        gender = gender,
        sexuality = sexuality,
        race = race,
        age = age,
        profession = profession,
        motivation = motivation,
        alignment = alignment,
        personalityTraits = personalityTraits,
        languages = languages,
        imagePath = null,
        notes = ""
    )
}
