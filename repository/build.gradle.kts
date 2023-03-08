import AppDependencies.implementation
plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
}

android {
    kapt {
        generateStubs = true
    }

    compileSdk = Version.compileSdk
    buildToolsVersion = Version.buildTool

    defaultConfig {
        minSdkPreview = Config.minSdkVersion
        targetSdkPreview = Config.targetVersion
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
    implementation(project(AppDependencies.db))


    implementation(project(":loger"))
    implementation(project(":model"))
    implementation(project(":common"))
}