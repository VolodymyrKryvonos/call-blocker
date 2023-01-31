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
    }
}

dependencies {
    implementation (AppDependencies.ktx)

    implementation ("com.jakewharton.timber:timber:5.0.1")

}