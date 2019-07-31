package me.kerooker.rpgnpcgenerator

import android.widget.TextView
import androidx.test.core.app.launchActivity
import io.kotlintest.Spec
import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.extensions.ConstructorExtension
import io.kotlintest.extensions.TestListener
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import org.junit.Test
import org.junit.runners.model.FrameworkMethod
import org.robolectric.RobolectricTestRunner
import org.robolectric.internal.bytecode.InstrumentationConfiguration
import kotlin.reflect.KClass

class ExampleUnitTest : FunSpec() {

    private val containedRobolectricRunner = ContainedRobolectricRunner()

    override fun listeners() = listOf(RobolectricListener(containedRobolectricRunner))

    init {
        test("Basic MainActivity test") {
            val act = launchActivity<MainActivity>()

            act.onActivity {
                it.findViewById<TextView>(R.id.foo).text.toString() shouldBe "Hello World!"
            }
        }
    }
}

class RobolectricConstructorExtension : ConstructorExtension {

    private val containedRobolectricRunner = ContainedRobolectricRunner()

    override fun <T : Spec> instantiate(clazz: KClass<T>): Spec? {
        return containedRobolectricRunner.sdkEnvironment.bootstrappedClass<Spec>(clazz.java).newInstance()
    }

}

class RobolectricListener(
    private val containedRobolectricRunner: ContainedRobolectricRunner
) : TestListener {

    override fun beforeTest(testCase: TestCase) {
        Thread.currentThread().contextClassLoader = containedRobolectricRunner.sdkEnvironment.robolectricClassLoader
        containedRobolectricRunner.containedBefore()
    }

    override fun afterTest(testCase: TestCase, result: TestResult) {
        containedRobolectricRunner.containedAfter()
        Thread.currentThread().contextClassLoader = RobolectricListener::class.java.classLoader
    }
}

class ContainedRobolectricRunner : RobolectricTestRunner(PlaceholderTest::class.java) {


    val placeHolderMethod by lazy {
        children[0]
    }

    val bootStrappedMethod by lazy {
        sdkEnvironment.bootstrappedClass<Any>(testClass.javaClass).getMethod(placeHolderMethod.name)

    }

    val sdkEnvironment by lazy {
        getSandbox(placeHolderMethod).also {
            configureSandbox(it, placeHolderMethod)
        }
    }

    fun containedBefore() {
        super.beforeTest(sdkEnvironment, placeHolderMethod, bootStrappedMethod)
    }

    fun containedAfter() {
        super.afterTest(placeHolderMethod, bootStrappedMethod)
    }

    override fun createClassLoaderConfig(method: FrameworkMethod?): InstrumentationConfiguration {
        return InstrumentationConfiguration.Builder(super.createClassLoaderConfig(method))
            .doNotAcquireClass(this::class.java)
            .doNotAcquirePackage("io.kotlintest")
            .build()
    }

    class PlaceholderTest {
        @Test
        fun testPlaceholder() {
        }
    }
}