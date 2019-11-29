import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.android.build.gradle.internal.dsl.DefaultConfig
import com.github.triplet.gradle.play.PlayPublisherExtension
import groovy.lang.Closure
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import java.util.*

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
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

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    setupSigningConfigs()
    setupBuildTypes()
    setupFlavors()
    setupTests()

    packagingOptions {
        exclude("META-INF/LICENSE.md")
        exclude("META-INF/LICENSE-notice.md")
    }
    
    dataBinding { isEnabled = true }
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
            testLogging.showStackTraces = true
            testLogging.exceptionFormat = FULL
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
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.50")

    // Android
    implementation("androidx.core:core-ktx:1.2.0-alpha03")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.0.0")
    implementation("com.google.android.material:material:1.1.0-alpha09")
    implementation("androidx.recyclerview:recyclerview:1.1.0-beta03")

    // Android Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:$navigationVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navigationVersion")
    
    // Memory leak detection
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.0-beta-3")

    // Koin
    implementation("org.koin:koin-android:$koinVersion")
    implementation("org.koin:koin-android-viewmodel:$koinVersion")
    testImplementation("io.kotlintest:kotlintest-extensions-koin:$kotlinTestVersion")

    // JSON
    implementation("com.beust:klaxon:5.0.1")

    // Object Box
    releaseImplementation("io.objectbox:objectbox-android:$objectBoxVersion")
    implementation("io.objectbox:objectbox-kotlin:$objectBoxVersion")
    debugImplementation("io.objectbox:objectbox-android-objectbrowser:$objectBoxVersion")
    kapt("io.objectbox:objectbox-processor:$objectBoxVersion")

    // Keyboard manipulation
    implementation("net.yslibrary.keyboardvisibilityevent:keyboardvisibilityevent:2.3.0")
    
    // Testing
    testImplementation("io.kotlintest:kotlintest-runner-junit5:$kotlinTestVersion")
    testImplementation("org.robolectric:robolectric:4.3")
    testImplementation("androidx.test:core:1.2.0")
    testImplementation("androidx.test:core-ktx:1.2.0")
    testImplementation("junit:junit:4.12")
    testImplementation("io.mockk:mockk:1.9.3")

    // UI Testing
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
    androidTestImplementation("io.kotlintest:kotlintest-runner-junit4:$kotlinTestVersion")
    androidTestImplementation("androidx.test:core:1.2.0")
    androidTestImplementation("androidx.test:core-ktx:1.2.0")
    debugImplementation("androidx.fragment:fragment-testing:1.2.0-alpha02") {
        exclude("androidx.test", "core")
    }

}

// Must necesseraly be after the dependencies block as per documentation
apply {
    plugin("io.objectbox")
}