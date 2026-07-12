package me.kerooker.rpgnpcgenerator.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.kerooker.rpgnpcgenerator.viewmodel.my.npc.sanitizeTags

class NpcRepository(
    private val database: NpcDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val queries get() = database.npcQueries
    private val tagQueries get() = database.npcTagQueries

    fun all(): Flow<List<Npc>> = queries.selectAll().asFlow().mapToList(dispatcher)

    fun get(id: Long): Flow<Npc?> = queries.selectById(id).asFlow().mapToOneOrNull(dispatcher)

    /** Distinct, non-blank campaign names currently in use, ordered case-insensitively. */
    fun distinctCampaigns(): Flow<List<String>> =
        queries.selectDistinctCampaigns().asFlow().mapToList(dispatcher).map { it.filterNotNull() }

    /** Every NPC's tags, keyed by NPC id — drives the roster list's tag chips and tag search. */
    fun allTags(): Flow<Map<Long, List<String>>> =
        tagQueries.selectAllTags().asFlow().mapToList(dispatcher)
            .map { rows -> rows.groupBy({ it.npcId }, { it.tag }) }

    /** The tags on a single NPC, ordered case-insensitively. */
    fun tagsFor(id: Long): Flow<List<String>> =
        tagQueries.selectTagsForNpc(id).asFlow().mapToList(dispatcher)

    /** Distinct, non-blank tags currently in use, ordered case-insensitively (autocomplete source). */
    fun distinctTags(): Flow<List<String>> =
        tagQueries.selectDistinctTags().asFlow().mapToList(dispatcher)

    /** Replaces the given NPC's tags with [tags] (cleaned via [sanitizeTags]) in one transaction. */
    fun setTags(npcId: Long, tags: List<String>) = database.transaction {
        tagQueries.deleteTagsForNpc(npcId)
        sanitizeTags(tags).forEach { tagQueries.insertTag(npcId, it) }
    }

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
            challengeRating = npc.challengeRating,
            campaign = npc.campaign,
            items = npc.items
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
            challengeRating = npc.challengeRating,
            campaign = npc.campaign,
            items = npc.items
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
            campaign = npc.campaign,
            items = npc.items,
            id = npc.id
        )
    }

    fun delete(id: Long) = database.transaction {
        tagQueries.deleteTagsForNpc(id)
        queries.deleteById(id)
    }
}
