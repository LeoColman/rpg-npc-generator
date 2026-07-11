package me.kerooker.rpgnpcgenerator.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

class NpcRepository(
    private val database: NpcDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val queries get() = database.npcQueries

    fun all(): Flow<List<Npc>> = queries.selectAll().asFlow().mapToList(dispatcher)

    fun get(id: Long): Flow<Npc?> = queries.selectById(id).asFlow().mapToOneOrNull(dispatcher)

    fun insert(npc: Npc): Long = database.transactionWithResult {
        queries.insert(
            fullName = npc.fullName,
            nickname = npc.nickname,
            gender = npc.gender,
            sexuality = npc.sexuality,
            race = npc.race,
            age = npc.age,
            profession = npc.profession,
            motivation = npc.motivation,
            alignment = npc.alignment,
            personalityTraits = npc.personalityTraits,
            languages = npc.languages,
            imagePath = npc.imagePath,
            notes = npc.notes,
            strength = npc.strength,
            dexterity = npc.dexterity,
            constitution = npc.constitution,
            intelligence = npc.intelligence,
            wisdom = npc.wisdom,
            charisma = npc.charisma,
            armorClass = npc.armorClass,
            hitPoints = npc.hitPoints,
            challengeRating = npc.challengeRating
        )
        queries.lastInsertRowId().executeAsOne()
    }

    fun insertWithId(npc: Npc) {
        queries.insertWithId(
            id = npc.id,
            fullName = npc.fullName,
            nickname = npc.nickname,
            gender = npc.gender,
            sexuality = npc.sexuality,
            race = npc.race,
            age = npc.age,
            profession = npc.profession,
            motivation = npc.motivation,
            alignment = npc.alignment,
            personalityTraits = npc.personalityTraits,
            languages = npc.languages,
            imagePath = npc.imagePath,
            notes = npc.notes,
            strength = npc.strength,
            dexterity = npc.dexterity,
            constitution = npc.constitution,
            intelligence = npc.intelligence,
            wisdom = npc.wisdom,
            charisma = npc.charisma,
            armorClass = npc.armorClass,
            hitPoints = npc.hitPoints,
            challengeRating = npc.challengeRating
        )
    }

    fun update(npc: Npc) {
        queries.update(
            fullName = npc.fullName,
            nickname = npc.nickname,
            gender = npc.gender,
            sexuality = npc.sexuality,
            race = npc.race,
            age = npc.age,
            profession = npc.profession,
            motivation = npc.motivation,
            alignment = npc.alignment,
            personalityTraits = npc.personalityTraits,
            languages = npc.languages,
            imagePath = npc.imagePath,
            notes = npc.notes,
            strength = npc.strength,
            dexterity = npc.dexterity,
            constitution = npc.constitution,
            intelligence = npc.intelligence,
            wisdom = npc.wisdom,
            charisma = npc.charisma,
            armorClass = npc.armorClass,
            hitPoints = npc.hitPoints,
            challengeRating = npc.challengeRating,
            id = npc.id
        )
    }

    fun delete(id: Long) = queries.deleteById(id)
}
