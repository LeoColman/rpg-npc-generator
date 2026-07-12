package me.kerooker.rpgnpcgenerator.viewmodel.my.npc.individual

import android.content.Context
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import io.kotest.assertions.nondeterministic.eventually
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import me.kerooker.rpgnpcgenerator.data.ListOfStringsAdapter
import me.kerooker.rpgnpcgenerator.data.Npc
import me.kerooker.rpgnpcgenerator.data.NpcDatabase
import me.kerooker.rpgnpcgenerator.data.NpcRepository
import me.kerooker.rpgnpcgenerator.repository.image.GeneratePortraitWorker
import me.kerooker.rpgnpcgenerator.ui.util.ImageStore
import kotlin.time.Duration.Companion.seconds

private const val NPC_ID = 42L
private const val OLD_IMAGE_PATH = "/data/user/0/me.kerooker.rpgnpcgenerator/files/portraits/old.jpg"
private const val NEW_IMAGE_PATH = "/data/user/0/me.kerooker.rpgnpcgenerator/files/portraits/new.jpg"
private const val GENERATED_IMAGE_PATH = "/data/user/0/me.kerooker.rpgnpcgenerator/files/portraits/generated.jpg"

// The viewmodel launches its DB/file work on the real Dispatchers.IO (hardcoded, not injected), so
// there is no TestDispatcher to advance for it. verify(timeout = ...) polls until the mocked call is
// observed instead of sleeping a fixed amount, which is the standard mockk idiom for this shape of
// fire-and-forget coroutine.
private const val VERIFY_TIMEOUT_MS = 2_000L

private fun sampleNpc(
    id: Long = NPC_ID,
    fullName: String = "Aria Nightsong",
    imagePath: String? = null,
    campaign: String? = null
) = Npc(
    id = id,
    fullName = fullName,
    nickname = "The Swift",
    gender = "Female",
    sexuality = "Heterosexual",
    race = "High Elf",
    age = "Adult",
    profession = "Ranger",
    motivation = "Protect the forest",
    alignment = "Neutral Good",
    personalityTraits = listOf("Brave", "Curious"),
    languages = listOf("Common", "Elvish"),
    imagePath = imagePath,
    notes = "Met in the tavern",
    strength = null,
    dexterity = null,
    constitution = null,
    intelligence = null,
    wisdom = null,
    charisma = null,
    armorClass = null,
    hitPoints = null,
    challengeRating = null,
    campaign = campaign,
    items = emptyList()
)

// Real in-memory repository (same setup as NpcRepositoryTest). Used by the delete tests because
// NpcRepository.delete() returns a SQLDelight value-class (QueryResult<Long>) that mockk 1.14.2
// cannot stub — driving the real DB is both simpler and closer to production behaviour.
private fun inMemoryRepository(): NpcRepository {
    val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
    NpcDatabase.Schema.create(driver)
    val database = NpcDatabase(
        driver = driver,
        npcAdapter = Npc.Adapter(
            personalityTraitsAdapter = ListOfStringsAdapter,
            languagesAdapter = ListOfStringsAdapter,
            itemsAdapter = ListOfStringsAdapter
        )
    )
    return NpcRepository(database, dispatcher = Dispatchers.Unconfined)
}

@OptIn(ExperimentalCoroutinesApi::class)
class IndividualNpcViewModelTest : FunSpec({

    beforeSpec {
        // viewModelScope requires Dispatchers.Main to be initialized on the JVM. Unconfined so that
        // stateIn's WhileSubscribed upstream collection starts eagerly once turbine subscribes,
        // instead of needing a manual advanceUntilIdle() call.
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    afterSpec {
        Dispatchers.resetMain()
    }

    afterTest {
        // mockkObject(...) state is global to the JVM, not scoped to a single test.
        unmockkAll()
    }

    test("npc mirrors the repository's flow for this id") {
        val repository = mockk<NpcRepository>()
        val repositoryFlow = MutableStateFlow<Npc?>(null)
        every { repository.get(NPC_ID) } returns repositoryFlow
        every { repository.distinctCampaigns() } returns MutableStateFlow(emptyList())
        every { repository.tagsFor(NPC_ID) } returns MutableStateFlow(emptyList())
        every { repository.distinctTags() } returns MutableStateFlow(emptyList())
        val context = mockk<Context>(relaxed = true)

        val viewModel = IndividualNpcViewModel(NPC_ID, repository, context)

        viewModel.npc.test {
            awaitItem() shouldBe null

            val npc = sampleNpc()
            repositoryFlow.value = npc
            awaitItem() shouldBe npc

            cancelAndIgnoreRemainingEvents()
        }
    }

    test("saveEdit updates the repository with the edited npc and deletes the previous portrait when it changed") {
        val repository = mockk<NpcRepository>()
        val repositoryFlow = MutableStateFlow<Npc?>(null)
        every { repository.get(NPC_ID) } returns repositoryFlow
        every { repository.distinctCampaigns() } returns MutableStateFlow(emptyList())
        every { repository.tagsFor(NPC_ID) } returns MutableStateFlow(emptyList())
        every { repository.distinctTags() } returns MutableStateFlow(emptyList())
        every { repository.update(any()) } just Runs
        val context = mockk<Context>(relaxed = true)
        mockkObject(ImageStore)
        every { ImageStore.deletePortrait(any(), any()) } just Runs

        val viewModel = IndividualNpcViewModel(NPC_ID, repository, context)

        viewModel.npc.test {
            awaitItem() shouldBe null
            val original = sampleNpc(imagePath = OLD_IMAGE_PATH)
            repositoryFlow.value = original
            awaitItem() shouldBe original

            viewModel.enableEdit()
            // A different id proves saveEdit forces id = npcId rather than trusting the argument.
            val edited = original.copy(id = 999L, fullName = "Nightsong the Second", imagePath = NEW_IMAGE_PATH)
            viewModel.saveEdit(edited)

            verify(timeout = VERIFY_TIMEOUT_MS) { repository.update(edited.copy(id = NPC_ID)) }
            verify(timeout = VERIFY_TIMEOUT_MS) { ImageStore.deletePortrait(context, OLD_IMAGE_PATH) }
            viewModel.editState.value shouldBe EditState.VIEW

            cancelAndIgnoreRemainingEvents()
        }
    }

    test("saveEdit does not delete the previous portrait when it is unchanged") {
        val repository = mockk<NpcRepository>()
        val repositoryFlow = MutableStateFlow<Npc?>(null)
        every { repository.get(NPC_ID) } returns repositoryFlow
        every { repository.distinctCampaigns() } returns MutableStateFlow(emptyList())
        every { repository.tagsFor(NPC_ID) } returns MutableStateFlow(emptyList())
        every { repository.distinctTags() } returns MutableStateFlow(emptyList())
        every { repository.update(any()) } just Runs
        val context = mockk<Context>(relaxed = true)
        mockkObject(ImageStore)
        every { ImageStore.deletePortrait(any(), any()) } just Runs

        val viewModel = IndividualNpcViewModel(NPC_ID, repository, context)

        viewModel.npc.test {
            awaitItem() shouldBe null
            val original = sampleNpc(imagePath = OLD_IMAGE_PATH)
            repositoryFlow.value = original
            awaitItem() shouldBe original

            val edited = original.copy(fullName = "Nightsong the Second")
            viewModel.saveEdit(edited)

            // update() and the (skipped) delete branch run synchronously in the same coroutine body
            // with no suspension point between them, so once update() is observed, the branch has
            // necessarily already been evaluated too — no race with the negative assertion below.
            verify(timeout = VERIFY_TIMEOUT_MS) { repository.update(edited.copy(id = NPC_ID)) }
            verify(exactly = 0) { ImageStore.deletePortrait(any(), any()) }

            cancelAndIgnoreRemainingEvents()
        }
    }

    test("saveEdit preserves a freshly generated portrait when the user did not change it in the editor") {
        val repository = mockk<NpcRepository>()
        val repositoryFlow = MutableStateFlow<Npc?>(null)
        every { repository.get(NPC_ID) } returns repositoryFlow
        every { repository.distinctCampaigns() } returns MutableStateFlow(emptyList())
        every { repository.tagsFor(NPC_ID) } returns MutableStateFlow(emptyList())
        every { repository.distinctTags() } returns MutableStateFlow(emptyList())
        every { repository.update(any()) } just Runs
        val context = mockk<Context>(relaxed = true)
        mockkObject(ImageStore)
        every { ImageStore.deletePortrait(any(), any()) } just Runs

        val viewModel = IndividualNpcViewModel(NPC_ID, repository, context)

        viewModel.npc.test {
            awaitItem() shouldBe null
            val original = sampleNpc(imagePath = OLD_IMAGE_PATH)
            repositoryFlow.value = original
            awaitItem() shouldBe original

            // Simulate the background worker finishing while the user is still editing.
            val generated = original.copy(imagePath = GENERATED_IMAGE_PATH)
            repositoryFlow.value = generated
            awaitItem() shouldBe generated

            viewModel.saveEdit(original.copy(fullName = "Edited Name", imagePath = OLD_IMAGE_PATH))

            verify(timeout = VERIFY_TIMEOUT_MS) {
                repository.update(original.copy(id = NPC_ID, fullName = "Edited Name", imagePath = GENERATED_IMAGE_PATH))
            }
            verify(exactly = 0) { ImageStore.deletePortrait(context, GENERATED_IMAGE_PATH) }

            cancelAndIgnoreRemainingEvents()
        }
    }

    test("saveEdit persists the edited tags when a tag list is provided") {
        val repository = mockk<NpcRepository>()
        val repositoryFlow = MutableStateFlow<Npc?>(null)
        every { repository.get(NPC_ID) } returns repositoryFlow
        every { repository.distinctCampaigns() } returns MutableStateFlow(emptyList())
        every { repository.tagsFor(NPC_ID) } returns MutableStateFlow(emptyList())
        every { repository.distinctTags() } returns MutableStateFlow(emptyList())
        every { repository.update(any()) } just Runs
        every { repository.setTags(any(), any()) } just Runs
        val context = mockk<Context>(relaxed = true)

        val viewModel = IndividualNpcViewModel(NPC_ID, repository, context)

        viewModel.npc.test {
            awaitItem() shouldBe null
            val original = sampleNpc()
            repositoryFlow.value = original
            awaitItem() shouldBe original

            viewModel.enableEdit()
            viewModel.saveEdit(original, tags = listOf("Villain", "Boss"))

            verify(timeout = VERIFY_TIMEOUT_MS) { repository.update(original.copy(id = NPC_ID)) }
            verify(timeout = VERIFY_TIMEOUT_MS) { repository.setTags(NPC_ID, listOf("Villain", "Boss")) }

            cancelAndIgnoreRemainingEvents()
        }
    }

    test("delete removes the npc and deletes its portrait image") {
        val repository = inMemoryRepository()
        repository.insertWithId(sampleNpc(id = NPC_ID, imagePath = OLD_IMAGE_PATH))
        val context = mockk<Context>(relaxed = true)
        mockkObject(ImageStore)
        mockkObject(GeneratePortraitWorker.Companion)
        every { ImageStore.deletePortrait(any(), any()) } just Runs
        every { GeneratePortraitWorker.cancel(any(), any()) } just Runs

        val viewModel = IndividualNpcViewModel(NPC_ID, repository, context)

        viewModel.npc.test {
            // stateIn starts at null; the DB flow then emits the stored npc (order may be conflated).
            var loaded = awaitItem()
            if (loaded == null) loaded = awaitItem()
            loaded!!.imagePath shouldBe OLD_IMAGE_PATH

            viewModel.delete()

            verify(timeout = VERIFY_TIMEOUT_MS) { GeneratePortraitWorker.cancel(context, NPC_ID) }
            verify(timeout = VERIFY_TIMEOUT_MS) { ImageStore.deletePortrait(context, OLD_IMAGE_PATH) }
            cancelAndIgnoreRemainingEvents()
        }
        // The row is actually gone from the DB.
        repository.get(NPC_ID).first() shouldBe null
    }

    test("delete does not touch the image store when the npc has no portrait") {
        val repository = inMemoryRepository()
        repository.insertWithId(sampleNpc(id = NPC_ID, imagePath = null))
        val context = mockk<Context>(relaxed = true)
        mockkObject(ImageStore)
        mockkObject(GeneratePortraitWorker.Companion)
        every { GeneratePortraitWorker.cancel(any(), any()) } just Runs

        val viewModel = IndividualNpcViewModel(NPC_ID, repository, context)

        viewModel.npc.test {
            var loaded = awaitItem()
            if (loaded == null) loaded = awaitItem()
            loaded!!.imagePath shouldBe null

            viewModel.delete()
            verify(timeout = VERIFY_TIMEOUT_MS) { GeneratePortraitWorker.cancel(context, NPC_ID) }
            cancelAndIgnoreRemainingEvents()
        }
        // Once the row is gone the delete coroutine has run past the (skipped) image branch.
        eventually(2.seconds) { repository.get(NPC_ID).first() shouldBe null }
        verify(exactly = 0) { ImageStore.deletePortrait(any(), any()) }
    }

    test("generatePortrait enqueues a portrait render for this npc") {
        val repository = mockk<NpcRepository>()
        every { repository.get(NPC_ID) } returns MutableStateFlow<Npc?>(null)
        every { repository.distinctCampaigns() } returns MutableStateFlow(emptyList())
        every { repository.tagsFor(NPC_ID) } returns MutableStateFlow(emptyList())
        every { repository.distinctTags() } returns MutableStateFlow(emptyList())
        val context = mockk<Context>(relaxed = true)
        mockkObject(GeneratePortraitWorker.Companion)
        every { GeneratePortraitWorker.enqueue(any(), any()) } just Runs

        val viewModel = IndividualNpcViewModel(NPC_ID, repository, context)

        viewModel.generatePortrait()

        verify { GeneratePortraitWorker.enqueue(context, NPC_ID) }
    }
})
