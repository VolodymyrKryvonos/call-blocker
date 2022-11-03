import AppDependencies.implementation

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

android {
    compileSdk = (Version.compileSdk)
    buildToolsVersion = (Version.buildTool)

    defaultConfig {
        minSdkPreview = (Config.minSdkVersion)
        targetSdk =  (Config.targetVersion.toInt())
        buildConfigField("String", "versionName", "\"${Config.versionName}\"")
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
    implementation (AppDependencies.base)

    implementation(project(":db"))

    implementation(AppDependencies.rest)
    implementation(project(":repository"))

    implementation (project(":loger"))
    implementation(project(mapOf("path" to ":model")))
}