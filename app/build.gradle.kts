import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.android.build.gradle.internal.dsl.DefaultConfig
import com.github.triplet.gradle.play.PlayPublisherExtension
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import java.util.*

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    id("com.github.triplet.play") version "2.3.0"
    id("net.thauvin.erik.gradle.semver") version "1.0.3-beta"
}

android {
    compileSdkVersion(29)

    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(29)

        setupVersion()
    }

    setupSigningConfigs()
    setupBuildTypes()
    setupFlavors()
}

fun DefaultConfig.setupVersion() {
    versionName = semver.semver
    versionCode = semver.major * 10_000 + semver.minor * 100 + semver.patch
}

fun BaseAppModuleExtension.setupSigningConfigs() {
    val localProps = Properties().apply { load(project.rootProject.file("local.properties").inputStream()) }

    signingConfigs {
        create("release") {
            storeFile = file(localProps["key_file_location"]!!)
            storePassword = localProps["keystore_password"].toString()
            keyAlias = localProps["key_alias"].toString()
            keyPassword = localProps["key_password"].toString()
        }
    }
}

fun BaseAppModuleExtension.setupBuildTypes() {
    buildTypes {
        getByName("release") {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }

        getByName("debug") {
            applicationIdSuffix = ".debug"
            resValue("string", "app_name", "RPG NPC Generator Debug")
        }
    }
}

fun BaseAppModuleExtension.setupFlavors() {
    flavorDimensions("version")

    productFlavors {
        create("free") {
            applicationId = "me.kerooker.rpgnpcgenerator"
            resValue("string", "app_name", "RPG NPC Generator")
        }
        create("pro") {
            applicationId = "me.kerooker.rpgnpcgeneratorpro"
            resValue("string", "app_name", "RPG NPC Generator PRO")

        }
    }
}

configure<PlayPublisherExtension> {
    track = "beta"
    serviceAccountCredentials = file("../local/play-store-key.json")
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib-jdk8", KotlinCompilerVersion.VERSION))

    // Android
    implementation("androidx.appcompat:appcompat:1.0.2")
    implementation("androidx.core:core-ktx:1.0.2")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
}