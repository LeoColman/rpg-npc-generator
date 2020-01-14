import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.android.build.gradle.internal.dsl.DefaultConfig
import com.github.triplet.gradle.play.PlayPublisherExtension
import groovy.lang.Closure
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmOptions
import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("android.extensions")
    kotlin("kapt")
    id("com.github.triplet.play") version "2.6.1"
    id("net.thauvin.erik.gradle.semver") version "1.0.4"
    id("io.gitlab.arturbosch.detekt").version("1.0.0-RC16")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.3.61"
    id("com.google.android.gms.oss-licenses-plugin")
    idea
}

apply(plugin = "androidx.navigation.safeargs.kotlin")
apply(plugin = "com.google.gms.google-services")


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

@Suppress("UNCHECKED_CAST", "MapGetWithNotNullAssertionOperator")
fun DefaultConfig.setupVersion() {
    val versionProps = Properties().apply {
        load(file("version.properties").inputStream())
    }.toMap() as Map<String, String>
    versionName = versionProps["version.semver"].toString()
    versionCode = (versionProps["version.major"]!!.toInt() * 10_000) + (versionProps["version.minor"]!!.toInt() * 100) + versionProps["version.patch"]!!.toInt()
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
            resValue("string", "app_name_en", "RPG NPC Generator Debug")
        }
    }
}

fun BaseAppModuleExtension.setupFlavors() {
    flavorDimensions("version")

    productFlavors {
        create("free") {
            applicationId = "me.kerooker.rpgcharactergenerator"
            resValue("string", "app_name_en", "RPG NPC Generator")
            resValue("string", "app_name_pt", "Gerador de NPC para RPG")
        }
        create("pro") {
            applicationId = "me.kerooker.rpgcharactergeneratorpro"
            resValue("string", "app_name_en", "RPG NPC Generator PRO")
            resValue("string", "app_name_pt", "Gerador de NPC para RPG PRO")
    
    
        }
    }
}

fun BaseAppModuleExtension.setupTests() {
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all(closureOf<Test> {
            useJUnitPlatform()
            testLogging.showStackTraces = true
            testLogging.showExceptions = true
            testLogging.showCauses = true
            testLogging.showStandardStreams = true
            testLogging.exceptionFormat = TestExceptionFormat.FULL
            testLogging.events = TestLogEvent.values().toSet()
            testLogging.exceptionFormat = FULL
        } as Closure<Test>)
    }
}

configure<PlayPublisherExtension> {
    track = "beta"
    defaultToAppBundles = true
    serviceAccountCredentials = file("../local/play-store-key.json")
}

configure<DetektExtension> {
    config = files("detekt-config.yml")
}

dependencies {
    // Kotlin
    implementation(kotlin("stdlib-jdk8", KotlinCompilerVersion.VERSION))
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.61")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:0.14.0")

    // Android
    implementation("androidx.core:core-ktx:1.2.0-rc01")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.1.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.1.0")
    implementation("com.google.android.material:material:1.2.0-alpha02")
    
    // AdMob
    implementation("com.google.android.gms:play-services-ads:18.3.0")
    
    // Splitties
    implementation("com.louiscad.splitties:splitties-alertdialog:3.0.0-alpha06")
    
    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.1.0")
    implementation("jp.wasabeef:recyclerview-animators:3.0.0")
    
    // List Item View
    implementation("com.lucasurbas:listitemview:1.1.1")
    
    // Settings
    implementation("androidx.preference:preference-ktx:1.1.0")
    
    // Open source libraries
    implementation("com.google.android.gms:play-services-oss-licenses:17.0.0")
    
    // Image loading
    implementation("com.github.dhaval2404:imagepicker:1.6")
    implementation("com.github.florent37:inline-activity-result-kotlin:1.0.1")
    implementation("io.coil-kt:coil:0.8.0")

    // Android Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:$navigationVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navigationVersion")
    
    // Memory leak detection
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.0-beta-3")

    // Koin
    implementation("org.koin:koin-android:$koinVersion")
    implementation("org.koin:koin-android-viewmodel:$koinVersion")
    testImplementation("io.kotest:kotest-extensions-koin:$kotestVersion")
    
    // Object Box
    releaseImplementation("io.objectbox:objectbox-android:$objectBoxVersion")
    implementation("io.objectbox:objectbox-kotlin:$objectBoxVersion")
    debugImplementation("io.objectbox:objectbox-android-objectbrowser:$objectBoxVersion")
    kapt("io.objectbox:objectbox-processor:$objectBoxVersion")
    
    // Key-value store
    implementation("com.tencent:mmkv:1.0.23")
    
    //Firebase
    implementation("com.google.firebase:firebase-analytics:17.2.1")
    
    
    // Testing
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
    testImplementation("org.robolectric:robolectric:4.3")
    testImplementation("io.kotest:kotest-extensions-robolectric:$kotestVersion")
    testImplementation("androidx.test:core:1.2.0")
    testImplementation("androidx.test:core-ktx:1.2.0")
    testImplementation("io.mockk:mockk:1.9.3")

    // UI Testing
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
    androidTestImplementation("io.kotlintest:kotlintest-runner-junit4:3.4.2") { exclude(module = "objenesis") }
    androidTestImplementation("androidx.test:core:1.2.0")
    androidTestImplementation("androidx.test:core-ktx:1.2.0")
    androidTestImplementation("io.mockk:mockk-android:1.9.3") { exclude(module = "objenesis") }
    androidTestImplementation("org.objenesis:objenesis:2.6")
    debugImplementation("androidx.fragment:fragment-testing:1.2.0-alpha02") {
        exclude("androidx.test", "core")
    }

}

// Must necesseraly be after the dependencies block as per documentation
apply {
    plugin("io.objectbox")
}