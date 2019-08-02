package io.kotlintest.robolectric

import io.kotlintest.Spec
import io.kotlintest.extensions.ConstructorExtension
import kotlin.reflect.KClass

class RobolectricConstructorExtension : ConstructorExtension {

    private val containedRobolectricRunner = ContainedRobolectricRunner()

    override fun <T : Spec> instantiate(clazz: KClass<T>): Spec? {
        return containedRobolectricRunner.sdkEnvironment.bootstrappedClass<Spec>(clazz.java).newInstance()
    }

}