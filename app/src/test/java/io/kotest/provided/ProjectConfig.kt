package io.kotest.provided

import io.kotest.AbstractProjectConfig
import io.kotest.experimental.robolectric.RobolectricExtension
import io.kotest.extensions.ProjectLevelExtension

object ProjectConfig : AbstractProjectConfig() {
    override fun extensions(): List<ProjectLevelExtension> {
        return listOf(RobolectricExtension())
    }
}