#!/usr/bin/env kotlin
@file:Repository("https://repo1.maven.org/maven2/")
@file:Repository("https://central.sonatype.com/repository/maven-snapshots/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:4.0.0")

@file:Repository("https://bindings.krzeminski.it")
@file:DependsOn("actions:checkout:v4")
@file:DependsOn("actions:setup-java:v4")
@file:DependsOn("actions:upload-artifact:v4")
@file:DependsOn("gradle:actions__setup-gradle:v4")
@file:DependsOn("reactivecircus:android-emulator-runner:v2")

import io.github.typesafegithub.workflows.actions.actions.Checkout
import io.github.typesafegithub.workflows.actions.actions.SetupJava
import io.github.typesafegithub.workflows.actions.actions.UploadArtifact
import io.github.typesafegithub.workflows.actions.gradle.ActionsSetupGradle
import io.github.typesafegithub.workflows.actions.reactivecircus.AndroidEmulatorRunner
import io.github.typesafegithub.workflows.domain.RunnerType.UbuntuLatest
import io.github.typesafegithub.workflows.domain.triggers.PullRequest
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.dsl.workflow

workflow(
  name = "Instrumented Tests",
  on = listOf(Push(), PullRequest()),
  sourceFile = __FILE__
) {
  job(id = "instrumented-test", runsOn = UbuntuLatest) {
    uses(name = "Checkout", action = Checkout())
    uses(
      name = "Set up JDK 21",
      action = SetupJava(javaVersion = "21", distribution = SetupJava.Distribution.Temurin)
    )
    uses(name = "Set up Gradle", action = ActionsSetupGradle())

    run(
      name = "Enable KVM group perms",
      command = """
        echo 'KERNEL=="kvm", GROUP="kvm", MODE="0666", OPTIONS+="static_node=kvm"' | sudo tee /etc/udev/rules.d/99-kvm4all.rules
        sudo udevadm control --reload-rules
        sudo udevadm trigger --name-match=kvm
      """.trimIndent()
    )

    uses(
      name = "Run instrumented tests",
      action = AndroidEmulatorRunner(
        apiLevel = "34",
        target = AndroidEmulatorRunner.Target.GoogleApis,
        arch = AndroidEmulatorRunner.Arch.X8664,
        script = "./gradlew connectedFdroidDebugAndroidTest"
      )
    )

    uses(
      name = "Upload reports on failure",
      `if` = "failure()",
      action = UploadArtifact(
        name = "instrumented-test-reports",
        path = listOf("app/build/reports/androidTests", "app/build/outputs/androidTest-results")
      )
    )
  }
}
