package me.kerooker.rpgnpcgenerator.viewmodel.random.npc

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.kerooker.rpgnpcgenerator.analytics.Analytics
import me.kerooker.rpgnpcgenerator.analytics.AnalyticsEvents
import me.kerooker.rpgnpcgenerator.data.Npc
import me.kerooker.rpgnpcgenerator.data.NpcRepository
import me.kerooker.rpgnpcgenerator.repository.image.PortraitPrompt
import me.kerooker.rpgnpcgenerator.repository.image.PortraitQueueClient
import me.kerooker.rpgnpcgenerator.repository.image.PortraitRequest
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.Age
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.CompleteNpcGenerator
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.GeneratedNpc
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.GeneratedNpcData
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.Language
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.NpcDataGenerator
import me.kerooker.rpgnpcgenerator.repository.model.random.npc.TemporaryRandomNpcRepository
import me.kerooker.rpgnpcgenerator.ui.util.ImageStore
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

@Suppress("TooManyFunctions")
class RandomNpcViewModel(
    private val context: Context,
    private val completeNpcGenerator: CompleteNpcGenerator,
    private val npcDataGenerator: NpcDataGenerator,
    private val temporaryRandomNpcRepository: TemporaryRandomNpcRepository,
    private val npcRepository: NpcRepository,
    private val queueClient: PortraitQueueClient,
    private val analytics: Analytics
) : ViewModel() {

    private val _portrait = MutableStateFlow(PortraitUiState())

    /** The portrait for the NPC currently on screen: spinner, queue position/ETA, and the result. */
    val portrait: StateFlow<PortraitUiState> = _portrait.asStateFlow()

    private val _lockedFields = MutableStateFlow<Set<LockableField>>(emptySet())

    /**
     * The fields the user has pinned with the padlock: [randomizeAll] keeps these at their current
     * value and only re-rolls the rest. In-memory only — locks reset when the app restarts.
     */
    val lockedFields: StateFlow<Set<LockableField>> = _lockedFields.asStateFlow()

    /** Flips the padlock on a field. A locked field is shielded from [randomizeAll] only. */
    fun toggleLock(field: LockableField) = _lockedFields.update { locks ->
        if (field in locks) locks - field else locks + field
    }

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

    // Declared after [data] so its flow chain sees the initialized StateFlow, not a null during init.
    init {
        observePortrait()
    }

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
        // A newly generated age may have an incompatible profession, so re-roll it to match — unless
        // the user has locked the profession, in which case their explicit choice wins over the
        // consistency side-effect (the same locked profession randomizeAll would also preserve).
        if (age.isNotSameGroupAsCurrentAge() && LockableField.PROFESSION !in _lockedFields.value) {
            randomizeProfession()
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

    fun randomizeProfession() = setProfession(generateProfessionForAge(data.value.age))

    /** Rolls a profession from the pool matching [age]'s group, so children never get adult jobs. */
    private fun generateProfessionForAge(age: String): String {
        val group = if (ageIsChild(age)) Age.Child else Age.Adult
        return npcDataGenerator.generateProfession(group)
    }

    private fun currentAgeIsChild(): Boolean = ageIsChild(data.value.age)

    private fun ageIsChild(age: String): Boolean {
        val childResource = context.getString(Age.Child.nameResource)
        return age.equals(childResource, true)
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

    /**
     * Rolls a fresh combat stats block. Combat columns are not read by [PortraitPrompt.forNpc] (see
     * the NOTE on [toNpc] below), so this never changes the built [PortraitRequest] and therefore
     * never triggers a portrait re-render.
     */
    fun randomizeCombatStats() =
        temporaryRandomNpcRepository.setCombat(completeNpcGenerator.generateCombatStats())

    fun setItem(index: Int, item: String) = temporaryRandomNpcRepository.setItem(index, item)

    fun removeItem(index: Int) = temporaryRandomNpcRepository.removeItem(index)

    /** Re-rolls a single item row (or appends one when [index] is past the end). */
    fun randomizeItem(index: Int) = setItem(index, completeNpcGenerator.generateSingleItem())

    /** Re-rolls the whole Items section as a fresh, profession-appropriate inventory. */
    fun randomizeAllItems() =
        temporaryRandomNpcRepository.setItems(completeNpcGenerator.generateItems(data.value.profession))

    fun randomizeAll() {
        analytics.capture(AnalyticsEvents.NPC_RANDOMIZED)
        temporaryRandomNpcRepository.setNpc(rollRespectingLocks())
    }

    /**
     * Rolls a full fresh NPC, then puts every locked field back to its current value. Nothing is
     * locked in the common case, so the merged row equals the freshly generated one.
     *
     * The age↔profession consistency rule needs care: a fresh NPC's profession matches its fresh
     * age group. If age is locked but profession isn't, that fresh profession could be wrong for the
     * kept age (an adult job on a locked Child), so we re-roll the profession against the locked age
     * group instead. A locked profession is always kept verbatim, whatever the age.
     */
    private fun rollRespectingLocks(): GeneratedNpcData {
        val locks = _lockedFields.value
        val current = data.value
        val fresh = completeNpcGenerator.generate().toNpcData()
        fun <T> pick(field: LockableField, kept: T, rolled: T) = if (field in locks) kept else rolled

        val age = pick(LockableField.AGE, current.age, fresh.age)
        val profession = when {
            LockableField.PROFESSION in locks -> current.profession
            LockableField.AGE !in locks -> fresh.profession // already matches the fresh age group
            else -> generateProfessionForAge(current.age) // age kept — match the profession to it
        }
        return fresh.copy(
            name = pick(LockableField.NAME, current.name, fresh.name),
            nickname = pick(LockableField.NICKNAME, current.nickname, fresh.nickname),
            race = pick(LockableField.RACE, current.race, fresh.race),
            age = age,
            gender = pick(LockableField.GENDER, current.gender, fresh.gender),
            sexuality = pick(LockableField.SEXUALITY, current.sexuality, fresh.sexuality),
            profession = profession,
            alignment = pick(LockableField.ALIGNMENT, current.alignment, fresh.alignment),
            motivation = pick(LockableField.MOTIVATION, current.motivation, fresh.motivation),
            personalityTraits = pick(LockableField.PERSONALITY, current.personalityTraits, fresh.personalityTraits),
            languages = pick(LockableField.LANGUAGES, current.languages, fresh.languages),
            combat = pick(LockableField.COMBAT, current.combat, fresh.combat),
            items = pick(LockableField.ITEMS, current.items, fresh.items)
        )
    }

    fun saveCurrentNpc() {
        val npc = data.value.toNpc()
        val portrait = _portrait.value.bitmap
        analytics.capture(
            AnalyticsEvents.NPC_SAVED,
            mapOf(AnalyticsEvents.PROP_HAS_PORTRAIT to (portrait != null))
        )
        viewModelScope.launch(Dispatchers.IO) {
            // Keep the portrait generated while rolling: persist the bitmap and store its path.
            val imagePath = portrait?.let { ImageStore.persistBitmap(context, it) }
            npcRepository.insert(npc.copy(imagePath = imagePath))
        }
    }

    /**
     * Re-renders the portrait whenever the on-screen NPC changes. Every re-roll (full or single
     * field) and every edit flows through [data]; [collectLatest] cancels the in-flight request and
     * starts a fresh one for the new options. [distinctUntilChanged] on the built [PortraitRequest]
     * skips re-rolls that don't change the prompt (name, nickname, motivation, languages), and
     * [debounce] collapses a burst of re-rolls into a single render.
     */
    @OptIn(FlowPreview::class)
    private fun observePortrait() {
        viewModelScope.launch {
            if (!queueClient.enabled()) return@launch
            data.map { PortraitPrompt.forNpc(it.toNpc()) }
                .debounce(PORTRAIT_DEBOUNCE_MS)
                .distinctUntilChanged()
                .collectLatest { generatePortrait(it) }
        }
    }

    private suspend fun generatePortrait(request: PortraitRequest) {
        _portrait.update { it.copy(isGenerating = true, failed = false, queueNumber = null, etaSeconds = null) }
        analytics.capture(
            AnalyticsEvents.PORTRAIT_REQUESTED,
            mapOf(AnalyticsEvents.PROP_SOURCE to AnalyticsEvents.SOURCE_RANDOMIZE)
        )
        val startedAtMs = SystemClock.elapsedRealtime()
        // Catch narrowly (not CancellationException): a re-roll cancels this via collectLatest, and
        // that must propagate so the next render takes over instead of showing a false failure.
        var jobId: String? = null
        try {
            val submitted = queueClient.submit(request)
            jobId = submitted.jobId
            publishQueue(submitted.ahead)
            val bitmap = awaitRender(submitted.jobId)
            jobId = null // finished — there's nothing left to cancel
            analytics.capture(
                AnalyticsEvents.PORTRAIT_GENERATED,
                mapOf(
                    AnalyticsEvents.PROP_SOURCE to AnalyticsEvents.SOURCE_RANDOMIZE,
                    AnalyticsEvents.PROP_QUEUE_AHEAD to submitted.ahead,
                    AnalyticsEvents.PROP_WAIT_SECONDS to (SystemClock.elapsedRealtime() - startedAtMs) / MS_PER_SECOND
                )
            )
            _portrait.update {
                it.copy(isGenerating = false, failed = false, bitmap = bitmap, queueNumber = null, etaSeconds = null)
            }
        } catch (e: CancellationException) {
            // On the JVM CancellationException IS an IllegalStateException, so it must be caught (and
            // re-thrown) BEFORE the IllegalStateException branch — a re-roll cancels us via
            // collectLatest, and that has to propagate, not surface as a false "failed" portrait.
            throw e
        } catch (e: IOException) {
            failPortrait(e) // server unreachable or HTTP error
        } catch (e: IllegalStateException) {
            failPortrait(e) // server reported an error, sent no image, or the render timed out
        } finally {
            // On a re-roll (cancellation) or a failure, free the still-queued job so it never renders.
            jobId?.let { cancelJob(it) }
        }
    }

    private fun failPortrait(cause: Exception) {
        Log.w(TAG, "Portrait generation failed", cause)
        analytics.capture(
            AnalyticsEvents.PORTRAIT_FAILED,
            mapOf(
                AnalyticsEvents.PROP_SOURCE to AnalyticsEvents.SOURCE_RANDOMIZE,
                AnalyticsEvents.PROP_REASON to (cause::class.simpleName ?: "unknown")
            )
        )
        _portrait.update { it.copy(isGenerating = false, failed = true, queueNumber = null, etaSeconds = null) }
    }

    /** Fires the server-side cancel even while this coroutine is being cancelled, so it reaches the queue. */
    private suspend fun cancelJob(jobId: String) {
        withContext(NonCancellable) { runCatching { queueClient.cancel(jobId) } }
    }

    /** Polls the queue until the render is done, updating queue position/ETA, mirroring the worker. */
    private suspend fun awaitRender(jobId: String): Bitmap {
        var elapsed = 0L
        var consecutiveErrors = 0
        while (elapsed < PORTRAIT_MAX_WAIT_MS) {
            val status = try {
                queueClient.status(jobId)
            } catch (e: IOException) {
                // One flaky poll shouldn't kill a multi-minute render; give up only after several.
                if (++consecutiveErrors >= PORTRAIT_MAX_POLL_ERRORS) throw e
                delay(PORTRAIT_POLL_INTERVAL_MS)
                elapsed += PORTRAIT_POLL_INTERVAL_MS
                continue
            }
            consecutiveErrors = 0
            when (status.state) {
                "done" -> {
                    val bytes = queueClient.decode(status.image ?: error("no image in response"))
                    return withContext(Dispatchers.Default) {
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: error("could not decode image")
                    }
                }
                "error" -> error(status.error ?: "render error")
                else -> publishQueue(status.ahead)
            }
            delay(PORTRAIT_POLL_INTERVAL_MS)
            elapsed += PORTRAIT_POLL_INTERVAL_MS
        }
        error("render timed out")
    }

    /** ETA is estimated as the number of renders ahead (plus ours) times the warm per-render time. */
    private fun publishQueue(ahead: Int) {
        _portrait.update { it.copy(queueNumber = ahead, etaSeconds = (ahead + 1) * SECONDS_PER_RENDER) }
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
            languages = languages.map { context.getString(it.nameResource) },
            combat = combat,
            items = items
        )
    }

    // NOTE: PortraitPrompt.forNpc ignores the combat columns, so threading them into the row here does
    // NOT change the built PortraitRequest — distinctUntilChanged still collapses combat-only edits and
    // no portrait re-render is triggered.
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
        notes = "",
        strength = combat?.strength?.toLong(),
        dexterity = combat?.dexterity?.toLong(),
        constitution = combat?.constitution?.toLong(),
        intelligence = combat?.intelligence?.toLong(),
        wisdom = combat?.wisdom?.toLong(),
        charisma = combat?.charisma?.toLong(),
        armorClass = combat?.armorClass?.toLong(),
        hitPoints = combat?.hitPoints?.toLong(),
        challengeRating = combat?.challengeRating,
        campaign = null,
        items = items
    )

    private companion object {
        private const val TAG = "RandomNpcViewModel"
        private const val PORTRAIT_DEBOUNCE_MS = 600L
        private const val PORTRAIT_POLL_INTERVAL_MS = 2_000L
        private const val PORTRAIT_MAX_WAIT_MS = 9 * 60_000L
        private const val PORTRAIT_MAX_POLL_ERRORS = 5
        private const val MS_PER_SECOND = 1_000L

        // Warm per-render wall time on the server, used only for the ETA estimate. ~15s reflects
        // cfg 1.0 / 2 steps (safety_checker handles NSFW, so no doubled negative pass).
        private const val SECONDS_PER_RENDER = 15
    }
}

/**
 * A field (or whole section) that can be padlocked so [RandomNpcViewModel.randomizeAll] leaves it
 * untouched. [LANGUAGES], [PERSONALITY], [COMBAT] and [ITEMS] lock their entire section at once.
 */
enum class LockableField {
    NAME,
    NICKNAME,
    RACE,
    AGE,
    GENDER,
    SEXUALITY,
    PROFESSION,
    ALIGNMENT,
    MOTIVATION,
    LANGUAGES,
    PERSONALITY,
    COMBAT,
    ITEMS
}

/** UI state for the Randomize screen's portrait: the render result plus live queue progress. */
data class PortraitUiState(
    val isGenerating: Boolean = false,
    val bitmap: Bitmap? = null,
    val queueNumber: Int? = null,
    val etaSeconds: Int? = null,
    val failed: Boolean = false
)
