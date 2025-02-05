plugins {
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.com.android.library)
}

android {
    namespace = "com.call_blocker.verification"
    compileSdk = libs.versions.compileSdk.get().toInt()
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        val major = libs.versions.major.get()
        val minor = libs.versions.minor.get()
        val patch = libs.versions.patch.get()
        minSdk = libs.versions.minSdk.get().toInt()
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
    implementation(libs.bundles.rest)
    implementation(platform(libs.koin.bom))
    implementation(libs.bundles.koin)
    implementation(project(":db"))
    implementation(project(":common"))
    implementation(project(":loger"))
    implementation(project(":common:rest"))
}