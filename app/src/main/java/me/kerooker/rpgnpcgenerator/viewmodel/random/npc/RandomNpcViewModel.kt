package me.kerooker.rpgnpcgenerator.viewmodel.random.npc

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
    private val queueClient: PortraitQueueClient
) : ViewModel() {

    private val _portrait = MutableStateFlow(PortraitUiState())

    /** The portrait for the NPC currently on screen: spinner, queue position/ETA, and the result. */
    val portrait: StateFlow<PortraitUiState> = _portrait.asStateFlow()

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
        val portrait = _portrait.value.bitmap
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
        if (!queueClient.enabled) return
        viewModelScope.launch {
            data.map { PortraitPrompt.forNpc(it.toNpc()) }
                .debounce(PORTRAIT_DEBOUNCE_MS)
                .distinctUntilChanged()
                .collectLatest { generatePortrait(it) }
        }
    }

    private suspend fun generatePortrait(request: PortraitRequest) {
        _portrait.update { it.copy(isGenerating = true, failed = false, queueNumber = null, etaSeconds = null) }
        // Catch narrowly (not CancellationException): a re-roll cancels this via collectLatest, and
        // that must propagate so the next render takes over instead of showing a false failure.
        var jobId: String? = null
        try {
            val submitted = queueClient.submit(request)
            jobId = submitted.jobId
            publishQueue(submitted.ahead)
            val bitmap = awaitRender(submitted.jobId)
            jobId = null // finished — there's nothing left to cancel
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
            combat = combat
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
        campaign = null
    )

    private companion object {
        private const val TAG = "RandomNpcViewModel"
        private const val PORTRAIT_DEBOUNCE_MS = 600L
        private const val PORTRAIT_POLL_INTERVAL_MS = 2_000L
        private const val PORTRAIT_MAX_WAIT_MS = 9 * 60_000L
        private const val PORTRAIT_MAX_POLL_ERRORS = 5

        // Warm per-render wall time on the server, used only for the ETA estimate. ~15s reflects
        // cfg 1.0 / 2 steps (safety_checker handles NSFW, so no doubled negative pass).
        private const val SECONDS_PER_RENDER = 15
    }
}

/** UI state for the Randomize screen's portrait: the render result plus live queue progress. */
data class PortraitUiState(
    val isGenerating: Boolean = false,
    val bitmap: Bitmap? = null,
    val queueNumber: Int? = null,
    val etaSeconds: Int? = null,
    val failed: Boolean = false
)
