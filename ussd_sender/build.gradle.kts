import AppDependencies.implementation

plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdk = (Version.compileSdk)
    buildToolsVersion = (Version.buildTool)

    defaultConfig {
        minSdkPreview = (Config.minSdkVersion)
        targetSdk = (Config.targetVersion.toInt())
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

dependencies {
    implementation(AppDependencies.base)
    implementation(project(mapOf("path" to ":loger")))
    implementation(project(mapOf("path" to ":common")))
    implementation(project(mapOf("path" to ":ussd-library-kotlin")))
}