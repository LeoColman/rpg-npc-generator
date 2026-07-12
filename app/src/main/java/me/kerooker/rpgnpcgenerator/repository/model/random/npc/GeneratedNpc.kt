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
    val languages: List<Language>,
    val combat: CombatStats,
    val items: List<String>
)

data class GeneratedNpcData(
    val name: String,
    val nickname: String,
    val gender: String,
    val sexuality: String,
    val race: String,
    val age: String,
    val profession: String,
    val motivation: String,
    val alignment: String,
    val personalityTraits: List<String>,
    val languages: List<String>,
    val combat: CombatStats? = null,
    val items: List<String> = emptyList()
)
