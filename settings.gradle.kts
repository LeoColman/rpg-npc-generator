pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "rpg-npc-generator"
include(":app")

// Server-side portrait queue proxy (Ktor / Kotlin-JVM). Lives with the rest of the renderer stack
// under server/portrait-renderer so deploy assets stay together, but builds as part of this project.
include(":portrait-queue")
project(":portrait-queue").projectDir = file("server/portrait-renderer/queue")
