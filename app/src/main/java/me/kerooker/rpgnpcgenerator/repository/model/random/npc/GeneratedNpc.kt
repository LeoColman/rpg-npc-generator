package me.kerooker.rpgnpcgenerator.repository.model.random.npc

data class GeneratedNpc(
    val name: String,
    val nickname: String,
    val gender: Gender,
    val sexuality: Sexuality,
    val race: Race,
    val age: Age,
    val profession: String,
    val motivation: String,
    val alignment: Alignment,
    val personalityTraits: List<String>,
    val languages: List<Language>
)

data class GeneratedNpcData(
    var name: String,
    var nickname: String,
    var gender: String,
    var sexuality: String,
    var race: String,
    var age: String,
    var profession: String,
    var motivation: String,
    var alignment: String,
    val personalityTraits: MutableList<String>,
    val languages: MutableList<String>
)
