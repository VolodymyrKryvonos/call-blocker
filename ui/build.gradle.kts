import AppDependencies.implementation

plugins {
    id("com.android.library")
    id("kotlin-android")
    id("kotlin-kapt")
}

android {
    buildFeatures {
        compose = true
    }

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

    composeOptions {
        kotlinCompilerExtensionVersion = Version.compose
    }
}

dependencies {
    implementation (AppDependencies.base)
    implementation (AppDependencies.kotlinUI)
}