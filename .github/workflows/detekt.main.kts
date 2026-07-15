#!/usr/bin/env kotlin
@file:Repository("https://repo1.maven.org/maven2/")
@file:Repository("https://central.sonatype.com/repository/maven-snapshots/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:4.0.0")

@file:Repository("https://bindings.krzeminski.it")
@file:DependsOn("actions:checkout:v4")
@file:DependsOn("actions:setup-java:v4")
@file:DependsOn("actions:upload-artifact:v4")
@file:DependsOn("gradle:actions__setup-gradle:v4")

import io.github.typesafegithub.workflows.actions.actions.Checkout
import io.github.typesafegithub.workflows.actions.actions.SetupJava
import io.github.typesafegithub.workflows.actions.actions.UploadArtifact
import io.github.typesafegithub.workflows.actions.gradle.ActionsSetupGradle
import io.github.typesafegithub.workflows.domain.RunnerType.UbuntuLatest
import io.github.typesafegithub.workflows.domain.triggers.PullRequest
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.dsl.workflow

workflow(
  name = "Detekt",
  on = listOf(Push(), PullRequest()),
  sourceFile = __FILE__
) {
  job(id = "detekt", runsOn = UbuntuLatest) {
    uses(name = "Checkout", action = Checkout())
    uses(
      name = "Set up JDK 21",
      action = SetupJava(javaVersion = "21", distribution = SetupJava.Distribution.Temurin)
    )
    uses(name = "Set up Gradle", action = ActionsSetupGradle())
    run(name = "Detekt", command = "./gradlew detekt")
    uses(
      name = "Upload reports on failure",
      `if` = "failure()",
      action = UploadArtifact(name = "detekt-reports", path = listOf("app/build/reports/detekt"))
    )
  }
}
