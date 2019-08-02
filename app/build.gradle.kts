import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.android.build.gradle.internal.dsl.DefaultConfig
import com.github.triplet.gradle.play.PlayPublisherExtension
import groovy.lang.Closure
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import java.util.*

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    id("com.github.triplet.play") version "2.3.0"
    id("net.thauvin.erik.gradle.semver") version "1.0.3-beta"
    id("io.gitlab.arturbosch.detekt").version("1.0.0-RC16")
}

android {
    compileSdkVersion(28)
    buildToolsVersion("29.0.0")
    setupKotlinCompiler()

    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(28)

        setupVersion()
    }

    setupSigningConfigs()
    setupBuildTypes()
    setupFlavors()
    setupTests()
}

fun BaseAppModuleExtension.setupKotlinCompiler() {
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        val opts = this as KotlinJvmOptions
        opts.jvmTarget = "1.8"
    }
}

fun DefaultConfig.setupVersion() {
    versionName = semver.semver
    versionCode = semver.major * 10_000 + semver.minor * 100 + semver.patch
}

fun BaseAppModuleExtension.setupSigningConfigs() {

    signingConfigs {
        create("release") {
            try {
                val localProps = Properties().apply { load(project.rootProject.file("local.properties").inputStream()) }
                storeFile = file(localProps["key_file_location"]!!)
                storePassword = localProps["keystore_password"].toString()
                keyAlias = localProps["key_alias"].toString()
                keyPassword = localProps["key_password"].toString()
            } catch (_: Throwable) { }
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

fun BaseAppModuleExtension.setupTests() {
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all(closureOf<Test> {
            useJUnitPlatform()
        } as Closure<Test>)
    }
}

configure<PlayPublisherExtension> {
    track = "beta"
    serviceAccountCredentials = file("../local/play-store-key.json")
}

configure<DetektExtension> {
    config = files("detekt-config.yml")
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib-jdk8", KotlinCompilerVersion.VERSION))

    // Android
    implementation("androidx.appcompat:appcompat:1.0.2")
    implementation("androidx.core:core-ktx:1.0.2")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")

    // Koin
    implementation("org.koin:koin-android:2.0.1")
    testImplementation("io.kotlintest:kotlintest-extensions-koin:$kotlinTestVersion")

    // JSON
    implementation("com.beust:klaxon:5.0.1")

    // Testing
    testImplementation("io.kotlintest:kotlintest-runner-junit5:$kotlinTestVersion")
    testImplementation("org.robolectric:robolectric:4.3")
    testImplementation("androidx.test:core:1.2.0")
    testImplementation("androidx.test:core-ktx:1.2.0")
    testImplementation("junit:junit:4.12")

}

