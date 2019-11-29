package me.kerooker.rpgnpcgenerator.view.random.npc

import android.content.Context
import android.text.Editable
import android.text.InputType.TYPE_CLASS_TEXT
import android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputEditText
import kotlinx.android.synthetic.main.randomnpc_element_view.view.*
import me.kerooker.rpgnpcgenerator.R
import me.kerooker.rpgnpcgenerator.view.util.animateRotation


interface ManualInputListener {
    fun onManualInput(text: String)
}

class RandomNpcElementView(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs) {
    
    var onRandomClick: () -> Unit = { }
    
    var onManualInput: ManualInputListener = object: ManualInputListener {
        override fun onManualInput(text: String) { }
    }
    
    var text
        get() = random_field_text.text.toString()
        set(value) {
            if(text == value) return
            random_field_text.setText(value)
        }
    
    init {
        View.inflate(context, R.layout.randomnpc_element_view, this)
        
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.RandomNpcElementView)
        random_field_text_layout.hint = attributes.getString(R.styleable.RandomNpcElementView_hint)
        random_field_text_layout.editText?.setText(text)
        attributes.recycle()
        
        prepareDiceClick()
        prepareTextListener()
    
        random_field_text.prepareWordWrap()
    }
    
    
    private fun prepareDiceClick() {
        random_field_dice.setOnClickListener {
            it.animateRotation {
                onRandomClick()
            }
        }
    }
    
    private fun prepareTextListener() {
        random_field_text.doAfterTextChanged { text: Editable? ->
            onManualInput.onManualInput(text?.toString() ?: "")
        }
    }
    
    @Suppress("MagicNumber")
    private fun EditText.prepareWordWrap() {
        inputType = TYPE_CLASS_TEXT or TYPE_TEXT_FLAG_CAP_SENTENCES
        setSingleLine(true)
        maxLines = 100
        setHorizontallyScrolling(false)
        imeOptions = EditorInfo.IME_ACTION_DONE
    }

    fun setHint(hint: String) {
        random_field_text_layout.hint = hint
    }
    
}

class ClearFocusEditText: TextInputEditText {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    
    override fun onKeyPreIme(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            clearFocus()
        }
        
        return super.onKeyPreIme(keyCode, event)
    }
}
