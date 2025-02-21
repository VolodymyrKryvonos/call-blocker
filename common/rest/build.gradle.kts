plugins {
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.com.android.library)
}

android {
    namespace = "com.call_blocker.common.rest"
    compileSdk = libs.versions.compileSdk.get().toInt()
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
    implementation(project(":loger"))
    implementation(project(":db"))
}