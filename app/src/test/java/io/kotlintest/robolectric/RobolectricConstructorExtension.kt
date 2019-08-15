package io.kotlintest.robolectric

import io.kotlintest.Spec
import io.kotlintest.extensions.ConstructorExtension
import kotlin.reflect.KClass
import kotlin.reflect.full.findAnnotation

class RobolectricConstructorExtension : ConstructorExtension {

    private val containedRobolectricRunner = ContainedRobolectricRunner()

    override fun <T : Spec> instantiate(clazz: KClass<T>): Spec? {
        if(clazz.findAnnotation<SkipRobolectric>() != null) return null
        return containedRobolectricRunner.sdkEnvironment.bootstrappedClass<Spec>(clazz.java).newInstance()
    }

}

annotation class SkipRobolectric