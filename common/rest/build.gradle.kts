import AppDependencies.implementation

plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    compileSdk = (Version.compileSdk)
    buildToolsVersion = (Version.buildTool)

    defaultConfig {
        minSdk = (Config.minSdkVersion.toInt())
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
    implementation(AppDependencies.rest)
    implementation(AppDependencies.koin)
    implementation(project(":loger"))
    implementation(project(":db"))
}