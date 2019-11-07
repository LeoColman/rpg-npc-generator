package me.kerooker.rpgnpcgenerator.view.random.npc

import android.content.Context
import android.graphics.Rect
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.randomnpc_element_list_view.view.add_item_button
import kotlinx.android.synthetic.main.randomnpc_element_list_view.view.add_item_text
import kotlinx.android.synthetic.main.randomnpc_element_list_view.view.list
import me.kerooker.rpgnpcgenerator.R

interface OnPositionedRandomizeClick {
    fun onRandomClick(index: Int)
}

interface OnPositionedDeleteClick {
    fun onDeleteClick(index: Int)
}

interface IndexedManualInputListener {
    fun onManualInput(index: Int, text: String)
}


class RandomNpcListView(
    context: Context,
    attrs: AttributeSet
) : ConstraintLayout(context, attrs) {

    private val listView by lazy { list }
    private val adapter by lazy { RandomNpcListAdapter(listView) }
    
    init {
        View.inflate(context, R.layout.randomnpc_element_list_view, this)
        

        val attributes = context.obtainStyledAttributes(attrs, R.styleable.RandomNpcListView)
        adapter.hint = attributes.getString(R.styleable.RandomNpcListView_list_hint)!!
        attributes.recycle()
        
        add_item_button.setOnClickListener { addItem() }
        add_item_text.setOnClickListener { addItem() }
    }
    
    private fun addItem() {
        val nextIndex = listView.childCount
        adapter.onPositionedRandomizeClick.onRandomClick(nextIndex)
        scrollToAddButton()
    }
    
    @Suppress("MagicNumber")
    private fun scrollToAddButton() {
        postDelayed(
            {
                val addButton = add_item_button
                val rect = Rect(0, 0, addButton.width, addButton.height)
                addButton.requestRectangleOnScreen(rect, false)
            }, 100
        )
    }
    
    fun setElements(elements: List<String>) {
        adapter.elements = elements
    }
    
    fun setOnPositionedRandomizeClick(onPositionedRandomizeClick: OnPositionedRandomizeClick) {
        adapter.onPositionedRandomizeClick = onPositionedRandomizeClick
    }
    
    fun setOnPositionedDeleteClick(onPositionedDeleteClick: OnPositionedDeleteClick) {
        adapter.onPositionedDeleteClick = onPositionedDeleteClick
    }
    
    fun setOnIndexedManualInputListener(indexedManualInputListener: IndexedManualInputListener) {
        adapter.indexedManualInputListener = indexedManualInputListener
    }
    
}

class RandomNpcListAdapter(
    private val listView: LinearLayout
) {
    
    lateinit var onPositionedRandomizeClick: OnPositionedRandomizeClick
    lateinit var onPositionedDeleteClick: OnPositionedDeleteClick
    lateinit var indexedManualInputListener: IndexedManualInputListener
    
    var hint: String = ""
    
    var elements: List<String> = emptyList()
    set(value) {
        field = value
        updateElements()
    }
    
    private fun updateElements() {
        elements.forEachIndexed { index, s ->
            updateOrCreate(index, s)
        }
        removeRemainingViews()
    }
    
    private fun updateOrCreate(index: Int, string: String) {
        val currentPosition = listView.getChildAt(index)
        if(currentPosition == null) create(index, string) else update(index, string)
    }
    
    private fun create(index: Int, string: String) {
        val view = createView(string)
        listView.addView(view, index)
    }
    
    private fun createView(string: String): View {
        val inflater = LayoutInflater.from(listView.context)
        val view =
            inflater.inflate(R.layout.randomnpc_element_list_view_item, listView, false) as RandomNpcElementListview
        view.prepareTexts(string)
        view.prepareListeners()
        return view
    }
    
    private fun update(index: Int, text: String) {
        val view = listView.getChildAt(index) as RandomNpcElementListview
        view.prepareTexts(text)
    }
    
    private fun RandomNpcElementListview.prepareTexts(text: String) {
        setText(text)
        setHint(hint)
    }
    
    private fun RandomNpcElementListview.prepareListeners() {
        onRandomClick = {
            val index = listView.indexOfChild(this)
            onPositionedRandomizeClick.onRandomClick(index)
        }
        
        onDeleteClick = {
            val index = listView.indexOfChild(this)
            listView.removeViewAt(index)
            onPositionedDeleteClick.onDeleteClick(index)
        }
        
        onManualInput = object : ManualInputListener {
            override fun onManualInput(text: String) {
                val index = listView.indexOfChild(this@prepareListeners)
                indexedManualInputListener.onManualInput(index, text)
            }
        }
    }
    
    private fun removeRemainingViews() {
        val elementsSize = elements.size
        val childrenSize = listView.childCount
        
        val difference = childrenSize - elementsSize
        
        if(difference <= 0) return
        
        repeat(difference) {
            val lastIndex = listView.childCount - 1
            listView.removeViewAt(lastIndex)
        }
    }
    
}
