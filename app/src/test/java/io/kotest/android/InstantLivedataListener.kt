package io.kotest.android

import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import io.kotest.Spec
import io.kotest.extensions.TestListener

class InstantLivedataListener : TestListener {
    
    override fun beforeSpec(spec: Spec) {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
            
            override fun executeOnMainThread(runnable: Runnable) = runnable.run()
            
            override fun postToMainThread(runnable: Runnable) = runnable.run()
            
            override fun isMainThread() = true
        }
        )
    }
    
    override fun afterSpec(spec: Spec) {
        ArchTaskExecutor.getInstance().setDelegate(null)
    }
}
