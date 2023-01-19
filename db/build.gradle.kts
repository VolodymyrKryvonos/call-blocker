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
        minSdk = (Config.minSdkVersion.toInt())
        minSdkPreview = (Config.minSdkVersion)
        targetSdk =  (Config.targetVersion.toInt())
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

    implementation ("androidx.room:room-runtime:2.3.0")
    implementation(project(":model"))
    kapt("androidx.room:room-compiler:2.3.0")
    implementation("androidx.room:room-ktx:2.3.0")

    implementation(AppDependencies.moshi)
    implementation(AppDependencies.security)

    implementation(AppDependencies.paged)
}