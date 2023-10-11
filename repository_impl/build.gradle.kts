@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed

plugins {
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.com.android.library)
}

android {
    namespace = "com.call_blocker.a_repository"
    compileSdk = libs.versions.compileSdk.get().toInt()
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        val major = libs.versions.major.get()
        val minor = libs.versions.minor.get()
        val patch = libs.versions.patch.get()
        buildConfigField("String", "versionName", "\"$major.$minor.$patch\"")
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {

    implementation(libs.bundles.kotlin.base)
    implementation(libs.koin)
    implementation(libs.bundles.rest)

    implementation(project(":db"))
    implementation(project(":repository"))
    implementation(project(":loger"))
    implementation(project(":model"))
    implementation(project(":common"))
    implementation(project(":common:rest"))
}