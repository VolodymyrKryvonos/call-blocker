plugins {
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.com.android.library)
}

android {
    namespace = "com.call_blocker.ussd_sender"
    compileSdk = libs.versions.compileSdk.get().toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    implementation(libs.bundles.kotlin.base)
    implementation(project(":loger"))
    implementation(project(":common"))
    implementation(project(":ussd-library-kotlin"))
}