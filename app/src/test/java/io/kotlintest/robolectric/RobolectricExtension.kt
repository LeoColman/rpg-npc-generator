package io.kotlintest.robolectric

import io.kotlintest.TestCase
import io.kotlintest.TestResult
import io.kotlintest.extensions.TestCaseExtension

class RobolectricExtension : TestCaseExtension {

    override suspend fun intercept(
        testCase: TestCase,
        execute: suspend (TestCase, suspend (TestResult) -> Unit) -> Unit,
        complete: suspend (TestResult) -> Unit
    ) {
        val containedRobolectricRunner = ContainedRobolectricRunner()

        beforeTest(containedRobolectricRunner)
        execute(testCase) { complete(it) }
        afterTest(containedRobolectricRunner)
    }

    private fun beforeTest(containedRobolectricRunner: ContainedRobolectricRunner) {
        Thread.currentThread().contextClassLoader = containedRobolectricRunner.sdkEnvironment.robolectricClassLoader
        containedRobolectricRunner.containedBefore()
    }

    private fun afterTest(containedRobolectricRunner: ContainedRobolectricRunner) {
        containedRobolectricRunner.containedAfter()
        Thread.currentThread().contextClassLoader = RobolectricExtension::class.java.classLoader
    }
}