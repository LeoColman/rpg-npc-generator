package me.kerooker.rpgnpcgenerator.data

import kotlinx.serialization.Serializable

/**
 * Serializable snapshot of the whole saved roster, written to / read from a user-chosen `.json`
 * backup file. Portraits travel inline as base64 (see [NpcBackup.portraitJpegBase64]) so a single
 * file is a complete, device-independent restore.
 *
 * [version] lets future readers detect and migrate older backups; bump [RosterBackupService.CURRENT_VERSION]
 * whenever the shape changes.
 */
@Serializable
data class RosterBackup(
    val version: Int,
    val npcs: List<NpcBackup>
)

/**
 * One NPC as stored in a backup. Mirrors [Npc] but drops the local `id` (fresh rows are assigned on
 * import) and the device-local `imagePath` (the portrait bytes travel in [portraitJpegBase64] and are
 * re-materialised into a new file on the importing device).
 */
@Serializable
data class NpcBackup(
    val fullName: String,
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
    val notes: String,
    val strength: Long? = null,
    val dexterity: Long? = null,
    val constitution: Long? = null,
    val intelligence: Long? = null,
    val wisdom: Long? = null,
    val charisma: Long? = null,
    val armorClass: Long? = null,
    val hitPoints: Long? = null,
    val challengeRating: String? = null,
    val campaign: String? = null,
    /** The NPC's starting inventory. Defaulted so backups written before items existed still load. */
    val items: List<String> = emptyList(),
    /** The NPC's portrait as a base64-encoded JPEG, or null when it has none. */
    val portraitJpegBase64: String? = null
)

/** Projects a stored [Npc] into a backup entry, carrying [portraitJpegBase64] for its portrait (if any). */
fun Npc.toBackup(portraitJpegBase64: String?): NpcBackup = NpcBackup(
    fullName = fullName,
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
    notes = notes,
    strength = strength,
    dexterity = dexterity,
    constitution = constitution,
    intelligence = intelligence,
    wisdom = wisdom,
    charisma = charisma,
    armorClass = armorClass,
    hitPoints = hitPoints,
    challengeRating = challengeRating,
    campaign = campaign,
    items = items,
    portraitJpegBase64 = portraitJpegBase64
)

/**
 * Rebuilds an insertable [Npc] from a backup entry. [id] is 0 so the database assigns a fresh one, and
 * [imagePath] points at the freshly-written portrait file (or null when the backup had no portrait).
 */
fun NpcBackup.toNpc(imagePath: String?): Npc = Npc(
    id = 0,
    fullName = fullName,
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
    imagePath = imagePath,
    notes = notes,
    strength = strength,
    dexterity = dexterity,
    constitution = constitution,
    intelligence = intelligence,
    wisdom = wisdom,
    charisma = charisma,
    armorClass = armorClass,
    hitPoints = hitPoints,
    challengeRating = challengeRating,
    campaign = campaign,
    items = items
)
