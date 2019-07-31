package io.kotlintest.provided

import io.kotlintest.AbstractProjectConfig
import io.kotlintest.extensions.ProjectLevelExtension
import me.kerooker.rpgnpcgenerator.RobolectricConstructorExtension

object ProjectConfig : AbstractProjectConfig() {
    override fun extensions(): List<ProjectLevelExtension> {
        return listOf(RobolectricConstructorExtension())
    }
}