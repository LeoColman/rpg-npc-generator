// Plugin versions come from the root build (declared `apply false`) so they don't clash with the
// Kotlin plugin the Android :app already puts on the shared classpath — hence no version here.
plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("io.ktor.plugin")
}

group = "me.kerooker.portrait"
version = "1.0.0"

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.logback.classic)

    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.ktor.client.mock)
    testImplementation(libs.kotlinx.coroutines.test)
}

kotlin { jvmToolchain(21) }

application { mainClass.set("me.kerooker.portrait.queue.ApplicationKt") }

ktor { fatJar { archiveFileName.set("queue-all.jar") } }

tasks.test { useJUnitPlatform() }
