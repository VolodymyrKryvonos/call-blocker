@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.com.android.library)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.call_blocker.db"
    compileSdk = libs.versions.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        val major = libs.versions.major.get()
        val minor = libs.versions.minor.get()
        val patch = libs.versions.patch.get()
        buildConfigField("String", "versionName", "\"$major.$minor.$patch\"")
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    implementation(libs.bundles.kotlin.base)
    implementation(libs.bundles.room)
    ksp(libs.androidx.room.compiler)
    implementation(libs.moshi)
    implementation(libs.koin)

    implementation(libs.androidx.security.crypto)
    implementation(project(":model"))
}