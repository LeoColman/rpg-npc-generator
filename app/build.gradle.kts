import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.detekt)
}

val versionProps = Properties().apply {
    file("version.properties").inputStream().use { load(it) }
}
val appVersionName: String = versionProps.getProperty("version.semver")
val appVersionCode: Int = versionProps.getProperty("version.major").toInt() * 10_000 +
    versionProps.getProperty("version.minor").toInt() * 100 +
    versionProps.getProperty("version.patch").toInt()

android {
    namespace = "me.kerooker.rpgnpcgenerator"
    compileSdk = 36

    defaultConfig {
        applicationId = "me.kerooker.rpgcharactergenerator"
        minSdk = 26
        targetSdk = 36
        versionCode = appVersionCode
        versionName = appVersionName

        // Server-side portrait renderer (ritalee). Password comes from a gradle property (e.g. in
        // ~/.gradle/gradle.properties or -PnpcImagePassword=...), never committed; empty disables
        // portrait generation entirely (there is no on-device path).
        buildConfigField(
            "String",
            "NPC_IMAGE_BASE_URL",
            "\"${providers.gradleProperty("npcImageBaseUrl").getOrElse("https://npc-fast.colman.com.br")}\""
        )
        buildConfigField("String", "NPC_IMAGE_USER", "\"${providers.gradleProperty("npcImageUser").getOrElse("npc")}\"")
        buildConfigField(
            "String",
            "NPC_IMAGE_PASSWORD",
            "\"${providers.gradleProperty("npcImagePassword").getOrElse("")}\""
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Release signing. The keystore and its passwords are kept out of the repo and revealed in CI
    // via git-secret (see app/keystore.secret / app/keystore.properties.secret). When the secrets
    // are not present (fresh clone, PR builds) the release variant simply stays unsigned.
    signingConfigs {
        val keystorePropertiesFile = file("keystore.properties")
        if (keystorePropertiesFile.exists()) {
            val keystoreProperties = Properties().apply {
                keystorePropertiesFile.inputStream().use { load(it) }
            }
            create("release") {
                storeFile = file(keystoreProperties.getProperty("STORE_FILE", "keystore"))
                storePassword = keystoreProperties.getProperty("KEYSTORE_PASSWORD")
                keyAlias = keystoreProperties.getProperty("SIGNING_KEY_ALIAS")
                keyPassword = keystoreProperties.getProperty("SIGNING_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.findByName("release")
        }
        debug {
            applicationIdSuffix = ".debug"
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    lint {
        warningsAsErrors = false
        abortOnError = true
        checkDependencies = true
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md",
                "META-INF/{AL2.0,LGPL2.1}",
            )
        }
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all { it.useJUnitPlatform() }
    }
}

kotlin {
    jvmToolchain(17)
}

sqldelight {
    databases {
        create("NpcDatabase") {
            packageName.set("me.kerooker.rpgnpcgenerator.data")
        }
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
    autoCorrect = providers.gradleProperty("detektAutoCorrect").map { it.toBoolean() }.getOrElse(false)
}

dependencies {
    // AndroidX core
    implementation(libs.androidx.core.ktx)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.core.splashscreen)

    // Background portrait generation (fire-and-forget + notification)
    implementation(libs.androidx.work.runtime.ktx)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Lifecycle + ViewModel for Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Image loading
    implementation(libs.coil.compose)

    // DI
    implementation(platform(libs.koin.bom))
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.compose)

    // Serialization + coroutines
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    // Persistence (SQLDelight)
    implementation(libs.sqldelight.android.driver)
    implementation(libs.sqldelight.coroutines.extensions)

    // Detekt formatting (ktlint rules)
    detektPlugins(libs.detekt.formatting)

    // Testing
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.sqldelight.sqlite.driver)

    // Compose UI tests under Robolectric (no emulator needed in CI)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.robolectric)
    testRuntimeOnly(libs.junit.vintage.engine)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
