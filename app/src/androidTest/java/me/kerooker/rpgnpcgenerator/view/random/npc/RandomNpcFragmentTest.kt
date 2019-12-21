package me.kerooker.rpgnpcgenerator.view.random.npc

import androidx.test.core.app.launchActivity
import io.kotlintest.IsolationMode
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.FunSpec
import kotlinx.android.synthetic.main.randomnpc_fragment.random_npc_fullname
import me.kerooker.rpgnpcgenerator.view.MainActivity

class RandomNpcFragmentTest : FunSpec() {
    
    
    init {
        test("Should not change currently generated values when fragment is created again") {
            var previousText = ""
            var afterText = ""
            val scenario = launchActivity<MainActivity>()
            
            scenario.onActivity {
                previousText = it.random_npc_fullname.text
            }
            
            scenario.recreate()
            
            scenario.onActivity {
                afterText = it.random_npc_fullname.text
            }
            
            previousText shouldBe afterText
            previousText shouldNotBe ""
        }
    }

    override fun isolationMode() = IsolationMode.InstancePerTest
}