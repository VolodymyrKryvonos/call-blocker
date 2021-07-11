import AppDependencies.implementation

plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-android")
    kotlin("kapt")
}

android {
    buildFeatures {
        compose = true
    }

    compileSdk = Version.compileSdk
    buildToolsVersion = Version.buildTool

    defaultConfig {
        applicationId = "com.callblocker.app"
        minSdk = Config.minSdkVersion.toInt()
        targetSdk = Config.targetVersion.toInt()
        versionCode = Config.versionCode
        versionName = Config.versionName
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

    buildTypes {
        release {
            applicationVariants.all {
                outputs.forEach { output ->
                    if (output is com.android.build.gradle.internal.api.BaseVariantOutputImpl) {
                        output.outputFileName =
                            "sms-sender-v${versionName}.${output.outputFile.extension}"
                    }
                }
            }
        }
    }
}

dependencies {
    implementation (AppDependencies.base)

    implementation (AppDependencies.kotlinUI)

    implementation("androidx.appcompat:appcompat:1.3.0")

    implementation ("androidx.fragment:fragment-ktx:1.3.5")

    implementation ( "androidx.compose.runtime:runtime-livedata:${Version.compose}")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-compose:1.0.0-alpha07")
    implementation ( "androidx.activity:activity-compose:1.3.0-rc01")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.3.1")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.3.1")
    implementation ("androidx.navigation:navigation-compose:2.4.0-alpha04")

    implementation (project(":ui"))

    implementation ("com.google.android.material:material:1.4.0")

    implementation (project(":repository"))
    implementation (project(":repository_imp"))

    implementation (project(":db"))

    implementation (AppDependencies.paged)

}