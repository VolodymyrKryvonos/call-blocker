@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed

plugins {

    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.crashlytics)
}

android {

    namespace = "com.call_blocker.app"
    signingConfigs {
        create("release") {
            storeFile = file("/Users/mykyta/Documents/callblockerunit/key")
            storePassword = "12345678"
            keyAlias = "nk"
            keyPassword = "12345678"
        }
    }
    buildFeatures {
        buildConfig = true
        compose = true
    }

    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.call_blocker.app"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.compileSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


        val major = libs.versions.major.get()
        val minor = libs.versions.minor.get()
        val patch = libs.versions.patch.get()
        versionCode = major.toInt() * 100000 + minor.toInt() * 1000 + patch.toInt()
        versionName = "$major.$minor.$patch"

        buildConfigField("int", "major", major)
        buildConfigField("int", "minor", minor)
        buildConfigField("int", "patch", patch)

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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.kotlinCompilerExtensionVersion.get()
    }

}

dependencies {
    implementation(libs.bundles.kotlin.base)
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.bundles.androidx)

    implementation(libs.moshi)
    implementation(libs.koin)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)

    implementation(project(":loger"))
    implementation(project(":repository"))
    implementation(project(":db"))
    implementation(project(":repository_impl"))
    implementation(project(":common"))
    implementation(project(":verification"))
    implementation(project(":model"))
    implementation(project(":ussd-library-kotlin"))
    implementation(project(":ussd_sender"))
}