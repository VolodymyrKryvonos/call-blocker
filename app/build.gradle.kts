import AppDependencies.implementation

plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    signingConfigs {
        create("release") {
            storeFile = file("/Users/mykyta/Documents/callblockerunit/key")
            storePassword = "12345678"
            keyAlias = "nk"
            keyPassword = "12345678"
        }
    }
    buildFeatures {
        compose = true
    }

    compileSdk = Version.compileSdk
    buildToolsVersion = Version.buildTool

    defaultConfig {
        applicationId = "com.call_blocker.app"
        minSdk = (Config.minSdkVersion.toInt())
        targetSdk = Config.targetVersion.toInt()
        versionCode = Config.versionCode
        versionName = Config.versionName
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("int", "major", "${Config.major}")
        buildConfigField("int", "minor", "${Config.minor}")
        buildConfigField("int", "patch", "${Config.patch}")
        buildConfigField("boolean", "showAmount", "true")
        buildConfigField("boolean", "logs", "true")
        buildConfigField("boolean", "sandBoxConfig", "false")
    }

    flavorDimensions += "version"

    productFlavors {
        create("bottega_sms") {
            resValue("string", "app_name", "Bottega SMS")
            applicationVariants.all {
                if (name.contains("bottega_sms")) {
                    outputs.forEach { output ->
                        if (output is com.android.build.gradle.internal.api.BaseVariantOutputImpl) {
                            output.outputFileName =
                                "new_ui_bottega-sms-sender-remote-v${versionName}.${output.outputFile.extension}"
                        }
                    }
                }
            }
        }

        create("sandbox") {
            buildConfigField("boolean", "sandBoxConfig", "true")
            resValue("string", "app_name", "Bottega SMS")
            applicationVariants.all {
                if (name.contains("sandbox")) {
                    outputs.forEach { output ->
                        if (output is com.android.build.gradle.internal.api.BaseVariantOutputImpl) {
                            output.outputFileName =
                                "new_ui_bottega-sms-sender-sandbox-v${versionName}.${output.outputFile.extension}"
                        }
                    }
                }
            }
        }

        create("asar") {
            resValue("string", "app_name", "ASAR")
            applicationVariants.all {
                if (name.contains("asar")) {
                    outputs.forEach { output ->
                        if (output is com.android.build.gradle.internal.api.BaseVariantOutputImpl) {
                            output.outputFileName =
                                "new_ui_asar-sms-sender-remote-v${versionName}.${output.outputFile.extension}"
                        }
                    }
                }
            }
        }
        create("without_amount") {
            resValue("string", "app_name", "SMS sender AN")
            buildConfigField("boolean", "showAmount", "false")
            buildConfigField("boolean", "logs", "false")
            applicationVariants.all {
                if (name.contains("without_amount")) {
                    outputs.forEach { output ->
                        if (output is com.android.build.gradle.internal.api.BaseVariantOutputImpl) {
                            output.outputFileName =
                                "new_ui_sms-sender-AN-remote-v${versionName}.${output.outputFile.extension}"
                        }
                    }
                }
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.2"
    }


}

dependencies {
    implementation(AppDependencies.base)

    implementation(AppDependencies.kotlinUI)

    implementation("androidx.appcompat:appcompat:1.6.1")

    implementation("androidx.fragment:fragment-ktx:1.5.5")

    implementation("androidx.compose.runtime:runtime-livedata:${Version.compose}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.5.1")
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.5.1")
    implementation("androidx.navigation:navigation-compose:2.5.3")

    implementation(AppDependencies.moshi)
    implementation(AppDependencies.koin)


    implementation("com.google.android.material:material:1.8.0")
    implementation(platform("com.google.firebase:firebase-bom:29.0.0"))

    implementation("com.google.firebase:firebase-analytics-ktx")

    implementation("com.google.firebase:firebase-crashlytics-ktx")

    implementation("androidx.work:work-runtime-ktx:2.8.0")

    implementation(project(":loger"))
    implementation(project(":repository"))
    implementation(project(":db"))
    implementation(project(":repository_impl"))
    implementation(project(":common"))
    implementation(project(":verification"))
    implementation(project(":model"))
    implementation(project(":ussd-library-kotlin"))
    implementation(project(mapOf("path" to ":ussd_sender")))
    testImplementation("junit:junit:4.12")
}