plugins {
    id("com.android.library")
    kotlin("android")
}

android {
    namespace = "com.call_blocker.model"

    defaultConfig {
        minSdkPreview = Config.minSdkVersion
        targetSdkPreview = Config.targetVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
    compileSdk = Version.compileSdk
    buildToolsVersion = Version.buildTool
}

dependencies {

    implementation (AppDependencies.ktx)
}