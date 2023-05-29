import org.gradle.api.JavaVersion.VERSION_1_8
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import java.util.Properties

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("io.gitlab.arturbosch.detekt").version("1.23.0")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.3.61"
}

android {
    namespace = "me.kerooker.rpgnpcgenerator"

    compileSdk = 33
    defaultConfig {
        minSdk = 21
        targetSdk = 33
        versionName = "4.0.0"
        versionCode = 40000
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = VERSION_1_8
        targetCompatibility = VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    signingConfigs {
        create("release") {
            try {
                val localProps = Properties().apply {
                    load(
                        project.rootProject.file("local.properties").inputStream()
                    )
                }
                storeFile = file(localProps["key_file_location"]!!)
                storePassword = localProps["keystore_password"].toString()
                keyAlias = localProps["key_alias"].toString()
                keyPassword = localProps["key_password"].toString()
            } catch (_: Throwable) {
            }
        }
    }
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

detekt {
    config.from("config/detekt/detekt-config.yaml")
    buildUponDefaultConfig = true
}


dependencies {
    // Android
    implementation("androidx.core:core-ktx:1.2.0-rc01")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.1.0")
    implementation("androidx.lifecycle:lifecycle-extensions:2.1.0")
    implementation("com.google.android.material:material:1.2.0-alpha02")

    // AdMob
    implementation("com.google.android.gms:play-services-ads:18.3.0")

    // Settings
    implementation("androidx.preference:preference-ktx:1.1.0")

    // Image loading
    implementation("com.github.dhaval2404:imagepicker:1.6")
    implementation("io.coil-kt:coil:0.8.0")

    // Android Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:$navigationVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navigationVersion")

    // Memory leak detection
    debugImplementation("com.squareup.leakcanary:leakcanary-android:2.0-beta-3")

    // Key-value store
    implementation("com.tencent:mmkv:1.2.16")


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