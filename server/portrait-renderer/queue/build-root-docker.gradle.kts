// Docker-only ROOT build script (copied to /repo/build.gradle.kts in the image build). The queue
// module applies its plugins WITHOUT versions; those versions are declared here `apply false`. In
// the container we build only :portrait-queue, so — unlike the real root build — we don't pull in
// AGP/sqldelight/detekt, keeping the image build Android-SDK-free and lean.
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.ktor) apply false
}
