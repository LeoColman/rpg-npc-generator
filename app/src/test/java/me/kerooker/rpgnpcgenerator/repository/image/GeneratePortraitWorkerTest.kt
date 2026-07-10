package me.kerooker.rpgnpcgenerator.repository.image

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.WorkManager
import androidx.work.testing.SynchronousExecutor
import androidx.work.testing.WorkManagerTestInitHelper
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.TimeUnit

/**
 * Covers [GeneratePortraitWorker.enqueue] against a real [WorkManager] initialized through
 * androidx.work:work-testing (which runs the WorkManager store synchronously and off the
 * main-thread-assert path). Verifies the contract the class's KDoc promises — one background job per
 * NPC, named "portrait_<id>", deduped by [androidx.work.ExistingWorkPolicy.KEEP].
 *
 * [GeneratePortraitWorker.doWork] is not exercised here: driving the CoroutineWorker end to end needs
 * work-testing's TestListenableWorkerBuilder plus a started Koin graph, out of scope for this test.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class GeneratePortraitWorkerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        val config = Configuration.Builder()
            .setExecutor(SynchronousExecutor())
            .setMinimumLoggingLevel(Log.DEBUG)
            .build()
        WorkManagerTestInitHelper.initializeTestWorkManager(context, config)
    }

    @Test
    fun `enqueue schedules a unique work request for the npc`() {
        GeneratePortraitWorker.enqueue(context, 42L)

        // Exactly one work item under this npc's unique name. (We don't assert the run state: the
        // test executor runs the worker, whose doWork needs a started Koin graph and so fails here —
        // scheduling the unique job is the part enqueue() actually owns.)
        val infos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork("portrait_42")
            .get(10, TimeUnit.SECONDS)

        infos.size shouldBe 1
    }

    @Test
    fun `enqueue twice for the same npc keeps a single unique work item`() {
        GeneratePortraitWorker.enqueue(context, 7L)
        GeneratePortraitWorker.enqueue(context, 7L)

        val infos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork("portrait_7")
            .get(10, TimeUnit.SECONDS)

        infos.size shouldBe 1
    }

    @Test
    fun `enqueue for different npc ids schedules independent work items`() {
        GeneratePortraitWorker.enqueue(context, 1L)
        GeneratePortraitWorker.enqueue(context, 2L)

        val workManager = WorkManager.getInstance(context)
        workManager.getWorkInfosForUniqueWork("portrait_1").get(10, TimeUnit.SECONDS).size shouldBe 1
        workManager.getWorkInfosForUniqueWork("portrait_2").get(10, TimeUnit.SECONDS).size shouldBe 1
    }
}
