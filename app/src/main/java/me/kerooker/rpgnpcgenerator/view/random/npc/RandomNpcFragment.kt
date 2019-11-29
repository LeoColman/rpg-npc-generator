package me.kerooker.rpgnpcgenerator.view.random.npc

import android.graphics.Bitmap
import android.graphics.Bitmap.Config
import android.graphics.Canvas
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import kotlinx.android.synthetic.main.activity_main.bottom_navigation_view
import kotlinx.android.synthetic.main.activity_main.toolbar
import kotlinx.android.synthetic.main.randomnpc_fragment.screenshot_view
import me.kerooker.rpgnpcgenerator.databinding.RandomnpcFragmentBinding
import me.kerooker.rpgnpcgenerator.viewmodel.random.npc.RandomNpcViewModel
import org.koin.android.viewmodel.ext.android.viewModel
import me.kerooker.rpgnpcgenerator.R
import me.kerooker.rpgnpcgenerator.view.util.animateRotation

class RandomNpcFragment : Fragment() {

    
    private val randomNpcViewModel by viewModel<RandomNpcViewModel>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }
    
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        
        val binding = RandomnpcFragmentBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.npc = randomNpcViewModel.data
        binding.randomNpcViewModel = randomNpcViewModel
        
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prepareOnRandomizeAllClick()
    }
    
    private fun prepareOnRandomizeAllClick() {
        val toolbar = activity!!.toolbar
        
        // This must be done through a LayoutChangeListener because it's the only way to get a reference to the toolbar
        // https://stackoverflow.com/questions/30787373/android-how-to-make-transition-animations-on-toolbars-menu-icons
        toolbar.addOnLayoutChangeListener(object : OnLayoutChangeListener {
            override fun onLayoutChange(v: View?, l: Int, t: Int, r: Int, b: Int, ol: Int, ot: Int, or: Int, ob: Int) {
                val item = toolbar.findViewById<View>(R.id.randomize_all) ?: return
                toolbar.removeOnLayoutChangeListener(this)
                item.setOnClickListener {
                    it.animateRotation {
                        onRandomizeAllMenuClick()
                    }
                }
            }
        })
    }
    
    
    
    
    private fun onRandomizeAllMenuClick() {
        randomNpcViewModel.randomizeAll()
    }
    
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_random_npc_fragment, menu)
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // This won't work because the animation of randomizeAll steals the onClick. The selection must happen there
            // R.id.randomize_all -> onRandomizeAllMenuClick()
            R.id.save          -> onSaveMenuClick()
        }
        return true
    }
    
    private fun onSaveMenuClick() {
        ScreenshotAnimator().animate()
        randomNpcViewModel.saveCurrentNpc()
    }
    
    private inner class ScreenshotAnimator {

        private val myNpcsFragmentView =
            requireActivity().bottom_navigation_view.findViewById<BottomNavigationItemView>(R.id.myNpcsFragment)
        
        fun animate() {
            if(isAnimating) return
            isAnimating = true
            val screenshot = takeScreenshot()
            screenshot_view.animateToBottom(screenshot)
        }
    
        private fun takeScreenshot(): Bitmap {
            val bitmap = Bitmap.createBitmap(requireView().width, requireView().height, Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            requireView().draw(canvas)
            return bitmap
        }
        
        private fun ImageView.animateToBottom(bitmap: Bitmap) {
            setImageBitmap(bitmap)
            visibility = View.VISIBLE
            
            shrinkingMoveTo(myNpcsFragmentView)
        }
        
        @Suppress("MagicNumber")
        private fun ImageView.shrinkingMoveTo(menuView: BottomNavigationItemView) {
            val (initialX, initialY) = x to y
            val (myX, myY) = getLocationInWindow()
            val (menuX, menuY) = menuView.getLocationInWindow()
    
            menuView.animateIconRotation()
            
            animate()
                .animateScaleFade()
                .animateMovement(myX, myY, menuX, menuY)
                .withEndAction { this.reset(initialX, initialY) }
                .setDuration(500L)
                .start()
        }
    
        private fun View.getLocationInWindow(): Pair<Int, Int> {
            val arr = IntArray(2)
            getLocationInWindow(arr)
        
            return arr[0] to arr[1]
        }
    
        private fun BottomNavigationItemView.animateIconRotation() = findViewById<View>(R.id.icon).animateRotation()
    
        @Suppress("MagicNumber")
        private fun ViewPropertyAnimator.animateScaleFade() = apply {
            alpha(0.05f)
            scaleX(0.05f)
            scaleY(0.05f)
        }
        
        private fun ViewPropertyAnimator.animateMovement(myX: Int, myY: Int, menuX: Int, menuY: Int) = apply {
            x((menuX - myX).toFloat())
            y((menuY - myY).toFloat())
        }
    
        @Suppress("MagicNumber")
        private fun ImageView.reset(initialX: Float, initialY: Float) {
            x = initialX
            y = initialY
            alpha = 1f
            scaleX = 1f
            scaleY = 1f
            isAnimating = false
            visibility = View.GONE
            setImageBitmap(null)
        }
    }
    
    companion object {
        private var isAnimating = false
    }
    
}
