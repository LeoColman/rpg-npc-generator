package me.kerooker.rpgnpcgenerator.view.random.npc

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import androidx.cardview.widget.CardView
import kotlinx.android.synthetic.main.randomnpc_element_list_view_item.view.*
import kotlinx.android.synthetic.main.randomnpc_element_view.view.random_field_dice

class RandomNpcElementListview(context: Context, attrs: AttributeSet) : CardView(context, attrs) {

    var onDeleteClick = {  }
    
    var onRandomClick = {  }
    
    var onManualInput = object : ManualInputListener {
        override fun onManualInput(text: String) {  }
    }
    
    override fun onFinishInflate() {
        super.onFinishInflate()
        prepareEventListeners()
    }

    private fun prepareEventListeners() {
        random_npc_minus.setOnClickListener { onDeleteClick() }
        random_npc_list_item_element.onRandomClick = { onRandomClick() }
        random_npc_list_item_element.onManualInput = object : ManualInputListener {
            override fun onManualInput(text: String) {
                onManualInput.onManualInput(text)
            }
        }
    }
    
    fun setText(text: String) { random_npc_list_item_element.text = text }

    fun setHint(hint: String) { random_npc_list_item_element.setHint(hint) }
}
