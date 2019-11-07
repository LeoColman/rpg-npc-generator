package me.kerooker.rpgnpcgenerator.view.util

import android.view.View
import android.view.animation.Animation
import android.view.animation.Animation.AnimationListener
import android.view.animation.AnimationUtils
import me.kerooker.rpgnpcgenerator.R

fun View.animateRotation(onAnimationEnd: () -> Unit = { }) {
    val animation = AnimationUtils.loadAnimation(context, R.anim.rotate_animation)
    animation.setAnimationListener(object: AnimationListener {
        override fun onAnimationRepeat(animation: Animation?) { }
    
        override fun onAnimationEnd(animation: Animation?) {
            onAnimationEnd()
        }
    
        override fun onAnimationStart(animation: Animation?) { }
    
    })
    startAnimation(animation)
}
