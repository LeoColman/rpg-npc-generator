package me.kerooker.rpgnpcgenerator.view.text

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import com.google.android.material.textfield.TextInputEditText

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
    
    /**
     * Necessary to remove underline on typos but keeping text suggestions
     * https://stackoverflow.com/a/36479831/4257162
     */
    override fun isSuggestionsEnabled() = false
}
