// Docker-only settings. The real build uses the repo-root settings.gradle.kts (which includes both
// :app and :portrait-queue). Inside the container we build ONLY the queue module, so we swap in this
// trimmed settings that never includes the Android :app — that keeps the image build free of the
// Android SDK. The `libs` version catalog is still picked up automatically from gradle/libs.versions.toml.
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "rpg-npc-generator"
include(":portrait-queue")
project(":portrait-queue").projectDir = file("server/portrait-renderer/queue")
