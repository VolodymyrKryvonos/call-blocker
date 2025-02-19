pluginManagement {
    repositories {
        maven("https://jitpack.io")
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven("https://jitpack.io")
        google()
        mavenCentral()
    }
}

rootProject.name = "call-blocker.unit"

include(":app")
include(":repository")
include(":loger")

include(":db")
include(":repository_impl")
include(":model")
include(":common")
include(":verification")
include(":common:rest")
include(":ussd_sender")
include(":ussd-library-kotlin")
include(":bottega-payment-request")
