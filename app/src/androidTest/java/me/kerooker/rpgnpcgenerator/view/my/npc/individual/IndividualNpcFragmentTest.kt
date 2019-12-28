package me.kerooker.rpgnpcgenerator.view.my.npc.individual

import android.view.MenuItem
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.replaceText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withText
import io.kotlintest.IsolationMode
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.specs.ShouldSpec
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.delay
import me.kerooker.rpgnpcgenerator.R
import me.kerooker.rpgnpcgenerator.repository.model.persistence.npc.NpcEntity
import me.kerooker.rpgnpcgenerator.repository.model.persistence.npc.NpcRepository
import org.koin.core.KoinComponent
import org.koin.core.inject

class IndividualNpcFragmentTest : ShouldSpec(), KoinComponent {
    
    private val npcRepository by inject<NpcRepository>()
    
    private var npcId: Long = -1
    
    init {
        should("Reset NPC fields when Edit Mode is cancelled") {
            val fragment =
                launchFragmentInContainer<IndividualNpcFragment>(IndividualNpcFragmentArgs(npcId).toBundle(), R.style.MyAppTheme)
            
            val item = mockk<MenuItem>()
            every { item.itemId }.returnsMany(R.id.individual_npc_edit, R.id.individual_npc_cancel)
            
            
            fragment.onFragment { it.onOptionsItemSelected(item) }
            onView(withText(dummyNpc.fullName)).perform(replaceText("New NPC Name"))
    
            fragment.onFragment { it.onOptionsItemSelected(item) }
            
            delay(100)
            onView(withText(dummyNpc.fullName)).check(matches(withText(dummyNpc.fullName)))
        }
        
        should("Not change fields when edit mode is saved") {
            val fragment =
                launchFragmentInContainer<IndividualNpcFragment>(IndividualNpcFragmentArgs(npcId).toBundle(), R.style.MyAppTheme)
    
            val item = mockk<MenuItem>()
            every { item.itemId }.returnsMany(R.id.individual_npc_edit, R.id.individual_npc_save)
    
    
            fragment.onFragment { it.onOptionsItemSelected(item) }
            onView(withText(dummyNpc.fullName)).perform(replaceText("New NPC Name"))
    
            fragment.onFragment { it.onOptionsItemSelected(item) }
    
            delay(100)
            onView(withText("New NPC Name")).check(matches(withText("New NPC Name")))
        }
    }
    
    override fun beforeTest(testCase: TestCase) {
        npcRepository.all().value?.forEach {
            npcRepository.delete(it)
        }
        npcId = npcRepository.put(dummyNpc)
    }
    
    override fun afterTest(testCase: TestCase, result: TestResult) {
        npcRepository.all().value?.forEach {
            npcRepository.delete(it)
        }
    }
    
    override fun isolationMode() = IsolationMode.InstancePerTest
}

private val dummyNpc = NpcEntity(
    "FullName",
    "Nickname",
    "Gender",
    "Sexuality",
    "Race",
    "Age",
    "Profession",
    "Motiation",
    "Alignment",
    emptyList(),
    emptyList(),
    imagePath = null
)