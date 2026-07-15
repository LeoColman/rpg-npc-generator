#!/usr/bin/env kotlin
@file:Repository("https://repo1.maven.org/maven2/")
@file:Repository("https://central.sonatype.com/repository/maven-snapshots/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:4.0.0")

@file:Repository("https://bindings.krzeminski.it")
@file:DependsOn("actions:checkout:v4")
@file:DependsOn("release-drafter:release-drafter:v5")

import io.github.typesafegithub.workflows.actions.releasedrafter.ReleaseDrafter_Untyped
import io.github.typesafegithub.workflows.domain.RunnerType.UbuntuLatest
import io.github.typesafegithub.workflows.domain.triggers.Push
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow

workflow(
  name = "Release Drafter",
  on = listOf(Push(branches = listOf("master"))),
  sourceFile = __FILE__
) {
  job(id = "update_release_draft", runsOn = UbuntuLatest) {
    uses(
      action = ReleaseDrafter_Untyped(),
      env = mapOf("GITHUB_TOKEN" to expr { secrets.GITHUB_TOKEN })
    )
  }
}
