buildscript {
    allprojects {
        repositories {
            google()
            mavenCentral()
        }
    }

    repositories {
        google()
        mavenCentral()
    }

    dependencies {
        classpath("com.android.tools.build:gradle:7.0.0")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.10")
        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.8.0")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}