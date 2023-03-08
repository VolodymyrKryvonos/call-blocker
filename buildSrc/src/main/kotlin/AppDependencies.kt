import org.gradle.api.artifacts.dsl.DependencyHandler

object AppDependencies {

    const val ktx = "androidx.core:core-ktx:${Version.ktx}"
    private const val kotlin_coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:${Version.kotlin_coroutines}"


    private const val security_crypto = "androidx.security:security-crypto:1.0.0"
    private const val security_identity_credential = "androidx.security:security-identity-credential:1.0.0-alpha02"

    val base = arrayListOf<String>().apply {
        add(ktx)
        add(kotlin_coroutines)
    }

    val security = arrayListOf<String>().apply {
        add(security_crypto)
        add(security_identity_credential)
    }

    val kotlinUI = arrayListOf<String>().apply {
        add("androidx.compose.ui:ui:${Version.compose}")
        // Foundation (Border, Background, Box, Image, Scroll, shapes, animations, etc.)
        add( "androidx.compose.foundation:foundation:${Version.compose}")
        // Material Design
        add( "androidx.compose.material:material:${Version.compose}")

        add("com.google.accompanist:accompanist-swiperefresh:0.14.0")

    }

    val moshi = listOf("com.squareup.moshi:moshi-kotlin:${Version.moshi}")

    val rest = arrayListOf<String>().apply {
        add("com.squareup.retrofit2:retrofit:2.9.0")
        add("com.squareup.retrofit2:converter-moshi:2.9.0")
        addAll(moshi)
        add(okhttp)
        add("com.squareup.okhttp3:logging-interceptor:4.9.1")
    }


    const val okhttp = "com.squareup.okhttp3:okhttp:4.9.1"

    val db = ":db"

    fun DependencyHandler.kapt(list: List<String>) {
        list.forEach { dependency ->
            add("kapt", dependency)
        }
    }

    fun DependencyHandler.implementation(list: List<String>) {
        list.forEach { dependency ->
            add("implementation", dependency)
        }
    }

}