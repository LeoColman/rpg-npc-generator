import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.detekt)
}

// AdMob test unit IDs (Google's public samples). Always used by debug builds, and the safe fallback
// for release builds that don't inject the real IDs via gradle properties (admobAppId /
// admobBannerUnitId / admobRewardedUnitId, e.g. in ~/.gradle/gradle.properties). Never knowingly
// serve these to production traffic.
val admobTestAppId = "ca-app-pub-3940256099942544~3347511713"
val admobTestBannerUnitId = "ca-app-pub-3940256099942544/6300978111"
val admobTestRewardedUnitId = "ca-app-pub-3940256099942544/5224354917"

// Real AdMob IDs (publisher pub-9745951044027822). Ad unit IDs are not secret — they ship inside the
// released APK — so they live in the repo for release builds. Debug always uses the test IDs above.
val admobRealAppId = "ca-app-pub-9745951044027822~3070790547"
val admobRealBannerUnitId = "ca-app-pub-9745951044027822/1209924287"
val admobRealRewardedUnitId = "ca-app-pub-9745951044027822/9457069649"

// PostHog project API key (client-side write-only token — it ships in the APK, like the AdMob IDs).
// Debug builds get no key so dev/emulator sessions never pollute production analytics; override via
// the posthogApiKey gradle property to test ingestion locally.
val posthogRealApiKey = "phc_vo4WK3ZPZDvYCmXDvvBgD9Ur5FTs3XDrMAaMBvixaRuU"

// GlitchTip DSN (self-hosted crash reporting on ritalee — see server/glitchtip/). A DSN is a
// write-only ingest key and is designed to ship inside the client, like the keys above. Debug builds
// get none, so local crashes stay local; override with -PglitchtipDsn=... to test reporting.
val glitchtipRealDsn = "https://23bf908f-9d11-4aef-871b-ad8370746b64@glitchtip.colman.com.br/1"

// Portrait-renderer basic-auth password. Unlike a real secret this credential ships in every released
// APK and is extractable (see RemoteImageConfig) — it only deters casual/bot abuse. It is committed here
// (like the AdMob/PostHog/GlitchTip keys above) so the F-Droid buildserver, which has no gradle secrets,
// still bakes a working default and portraits work out of the box on every flavor. Overridable with
// -PnpcImagePassword=... ; users can also point the app at their own self-hosted renderer in Settings.
val npcImageRealPassword = "Sm7fAu15qChx67lQkkkWjCwX"

android {
    namespace = "me.kerooker.rpgnpcgenerator"
    compileSdk = 36

    defaultConfig {
        applicationId = "me.kerooker.rpgcharactergenerator"
        minSdk = 26
        targetSdk = 36
        // Kept as literals (not computed) so F-Droid can parse them for tag-based auto-updates;
        // app/bump_version.sh rewrites these two lines. versionCode = major*10000 + minor*100 + patch.
        versionCode = 51103
        versionName = "5.11.3"

        // Server-side portrait renderer (ritalee). Password comes from a gradle property (e.g. in
        // ~/.gradle/gradle.properties or -PnpcImagePassword=...), never committed; empty disables
        // portrait generation entirely (there is no on-device path).
        buildConfigField(
            "String",
            "NPC_IMAGE_BASE_URL",
            "\"${providers.gradleProperty("npcImageBaseUrl").getOrElse("https://npc-fast.colman.com.br")}\""
        )
        buildConfigField("String", "NPC_IMAGE_USER", "\"${providers.gradleProperty("npcImageUser").getOrElse("npc")}\"")
        // Baked default for every flavor (incl. the F-Droid buildserver, which has no gradle props), so
        // portraits work out of the box; users can override the whole server in-app (Settings).
        buildConfigField(
            "String",
            "NPC_IMAGE_PASSWORD",
            "\"${providers.gradleProperty("npcImagePassword").getOrElse(npcImageRealPassword)}\""
        )

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Release signing. The keystore and its passwords are kept out of the repo and revealed in CI
    // via git-secret (see app/keystore.secret / app/keystore.properties.secret). When the secrets
    // are not present (fresh clone, PR builds) the release variant simply stays unsigned. Declared
    // before productFlavors so github/playstore can reference signingConfigs.findByName("release").
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

    // Release channels. `fdroid` is the FOSS build: no proprietary deps, no ads/analytics/crash, and no
    // signingConfig (F-Droid's buildserver signs it). `github` is an ad-free self-signed direct download
    // that keeps analytics + crash. `playstore` is the full Google Play build. Ads live only in
    // `playstore`; analytics + crash are shared by `github` + `playstore` via the src/telemetry source set.
    flavorDimensions += "distribution"
    productFlavors {
        create("fdroid") {
            dimension = "distribution"
            // Compiles src/main only (interfaces + no-ops). Portraits use the baked default password
            // above and stay runtime-editable via the in-app store.
        }
        create("github") {
            dimension = "distribution"
            signingConfig = signingConfigs.findByName("release")
            buildConfigField(
                "String",
                "POSTHOG_API_KEY",
                "\"${providers.gradleProperty("posthogApiKey").getOrElse(posthogRealApiKey)}\""
            )
            buildConfigField(
                "String",
                "GLITCHTIP_DSN",
                "\"${providers.gradleProperty("glitchtipDsn").getOrElse(glitchtipRealDsn)}\""
            )
        }
        create("playstore") {
            dimension = "distribution"
            signingConfig = signingConfigs.findByName("release")

            manifestPlaceholders["admobAppId"] =
                providers.gradleProperty("admobAppId").getOrElse(admobRealAppId)
            buildConfigField(
                "String",
                "ADMOB_BANNER_UNIT_ID",
                "\"${providers.gradleProperty("admobBannerUnitId").getOrElse(admobRealBannerUnitId)}\""
            )
            buildConfigField(
                "String",
                "ADMOB_REWARDED_UNIT_ID",
                "\"${providers.gradleProperty("admobRewardedUnitId").getOrElse(admobRealRewardedUnitId)}\""
            )
            buildConfigField(
                "String",
                "POSTHOG_API_KEY",
                "\"${providers.gradleProperty("posthogApiKey").getOrElse(posthogRealApiKey)}\""
            )
            buildConfigField(
                "String",
                "GLITCHTIP_DSN",
                "\"${providers.gradleProperty("glitchtipDsn").getOrElse(glitchtipRealDsn)}\""
            )
        }
    }

    // Analytics + crash reporting are shared by the two signed channels only (never fdroid). The
    // matching unit tests for that shared code live in src/testTelemetry, added to both test variants.
    sourceSets {
        named("github") { java.srcDirs("src/telemetry/java") }
        named("playstore") { java.srcDirs("src/telemetry/java") }
        named("testGithub") { java.srcDirs("src/testTelemetry/java") }
        named("testPlaystore") { java.srcDirs("src/testTelemetry/java") }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // signingConfig + the AdMob/PostHog/GlitchTip fields moved to the product flavors: signing +
            // analytics/crash belong to the signed channels (github/playstore), never the FOSS fdroid build.
        }
        debug {
            applicationIdSuffix = ".debug"

            // Debug always uses Google's test ad units — never real inventory.
            manifestPlaceholders["admobAppId"] = admobTestAppId
            buildConfigField("String", "ADMOB_BANNER_UNIT_ID", "\"$admobTestBannerUnitId\"")
            buildConfigField("String", "ADMOB_REWARDED_UNIT_ID", "\"$admobTestRewardedUnitId\"")

            // Blank key = analytics fully disabled (see AnalyticsModule).
            buildConfigField(
                "String",
                "POSTHOG_API_KEY",
                "\"${providers.gradleProperty("posthogApiKey").getOrElse("")}\""
            )
            // Blank DSN = crash reporting fully disabled (see CrashReporting).
            buildConfigField(
                "String",
                "GLITCHTIP_DSN",
                "\"${providers.gradleProperty("glitchtipDsn").getOrElse("")}\""
            )
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

    // Ads (AdMob) — bundles the UMP consent SDK (com.google.android.ump). playstore flavor only, so the
    // proprietary SDK never enters the fdroid/github classpaths. DataStore (ad-free entitlement + portrait
    // server config) is shared, so it stays a plain implementation.
    "playstoreImplementation"(libs.play.services.ads)
    implementation(libs.androidx.datastore.preferences)

    // Product analytics (PostHog) — the two signed channels only, never the FOSS fdroid build.
    "githubImplementation"(libs.posthog.android)
    "playstoreImplementation"(libs.posthog.android)

    // Crash reporting (self-hosted GlitchTip, Sentry protocol) — signed channels only.
    "githubImplementation"(libs.sentry.android)
    "playstoreImplementation"(libs.sentry.android)

    // Detekt formatting (ktlint rules)
    detektPlugins(libs.detekt.formatting)

    // Testing
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.sqldelight.sqlite.driver)
    testImplementation(libs.androidx.work.testing)

    // Compose UI tests under Robolectric (no emulator needed in CI)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.robolectric)
    // Kotest spec support for Robolectric (@RobolectricTest), see
    // https://github.com/LeoColman/kotest-android
    testImplementation(libs.kotest.extensions.android)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Compose UI tests on a real device/emulator (androidTest), mirroring the Robolectric ones above
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.kotest.assertions.core)
    // Kotest spec support for real instrumentation, see https://github.com/LeoColman/kotest-android
    androidTestImplementation(libs.kotest.runner.android)
}
