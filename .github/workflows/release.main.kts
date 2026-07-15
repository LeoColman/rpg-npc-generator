#!/usr/bin/env kotlin
@file:Repository("https://repo1.maven.org/maven2/")
@file:Repository("https://central.sonatype.com/repository/maven-snapshots/")
@file:DependsOn("io.github.typesafegithub:github-workflows-kt:4.0.0")

@file:Repository("https://bindings.krzeminski.it")
@file:DependsOn("actions:checkout:v4")
@file:DependsOn("actions:setup-java:v4")
@file:DependsOn("gradle:actions__setup-gradle:v4")
@file:DependsOn("entrostat:git-secret-action:v4")
@file:DependsOn("ruby:setup-ruby:v1")
@file:DependsOn("softprops:action-gh-release:v2")

import io.github.typesafegithub.workflows.actions.actions.Checkout
import io.github.typesafegithub.workflows.actions.actions.SetupJava
import io.github.typesafegithub.workflows.actions.entrostat.GitSecretAction
import io.github.typesafegithub.workflows.actions.gradle.ActionsSetupGradle
import io.github.typesafegithub.workflows.actions.ruby.SetupRuby
import io.github.typesafegithub.workflows.actions.softprops.ActionGhRelease
import io.github.typesafegithub.workflows.domain.RunnerType.UbuntuLatest
import io.github.typesafegithub.workflows.domain.triggers.WorkflowDispatch
import io.github.typesafegithub.workflows.domain.triggers.WorkflowDispatch.Input
import io.github.typesafegithub.workflows.domain.triggers.WorkflowDispatch.Input.Type.String
import io.github.typesafegithub.workflows.dsl.expressions.expr
import io.github.typesafegithub.workflows.dsl.workflow

workflow(
  name = "Release",
  on = listOf(
    WorkflowDispatch(
      inputs = mapOf(
        "version_type" to Input(
          description = "Type of version bump",
          required = true,
          type = WorkflowDispatch.Input.Type.Choice,
          options = listOf("major", "minor", "patch")
        ),
        "changelog" to Input(
          description = "Changelog for this release (use \\n for new lines)",
          required = true,
          type = String,
        )
      )
    )
  ),
  sourceFile = __FILE__
) {
  job(id = "release", runsOn = UbuntuLatest) {
    run(
      name = "Set up Git identity",
      command = """
        git config --global user.name "GitHub Actions"
        git config --global user.email "actions@github.com"
      """.trimIndent()
    )

    uses(
      name = "Checkout",
      action = Checkout(sshKey = expr { secrets["RELEASE_KEY"]!! })
    )

    uses(
      name = "Set up JDK 21",
      action = SetupJava(javaVersion = "21", distribution = SetupJava.Distribution.Temurin)
    )

    uses(name = "Set up Gradle", action = ActionsSetupGradle())

    uses(
      name = "Reveal secrets",
      action = GitSecretAction(gpgPrivateKey = expr { secrets["GPG_KEY"]!! })
    )

    val versionTypeExpr = expr { github["event.inputs.version_type"]!! }
    val changelogExpr = expr { github["event.inputs.changelog"]!! }
    val bumpStep = run(
      name = "Bump version and push tag",
      command = "./app/bump_version.sh \"$versionTypeExpr\" \"$changelogExpr\""
    )

    run(name = "Build release APK", command = "./gradlew assembleRelease")

    uses(
      name = "Create GitHub release",
      action = ActionGhRelease(
        name = expr { bumpStep.outputs["version"] },
        tagName = expr { bumpStep.outputs["version"] },
        draft = false,
        files = listOf(
          "app/build/outputs/apk/release/app-release.apk",
          "app/build/outputs/mapping/release/mapping.txt"
        )
      )
    )

    run(name = "Build Play Store bundle", command = "./gradlew bundleRelease")

    uses(
      name = "Set up Ruby",
      action = SetupRuby(
        rubyVersion = "3.3",
        bundlerCache = true,
        workingDirectory = "fastlane"
      )
    )

    run(
      name = "Publish to Google Play",
      command = "bundle exec fastlane playstore",
      env = mapOf("BUNDLE_GEMFILE" to "fastlane/Gemfile")
    )
  }
}
