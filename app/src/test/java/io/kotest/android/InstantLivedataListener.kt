package io.kotest.android

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import io.kotest.TestCase
import io.kotest.TestResult
import io.kotest.extensions.TestListener

class InstantLivedataListener : TestListener {
    
    override fun beforeTest(testCase: TestCase) {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
            
            override fun executeOnMainThread(runnable: Runnable) = runnable.run()
            
            override fun postToMainThread(runnable: Runnable) = runnable.run()
            
            override fun isMainThread() = true
        }
        )
    }
    
    override fun afterTest(testCase: TestCase, result: TestResult) {
        ArchTaskExecutor.getInstance().setDelegate(null)
    }
}