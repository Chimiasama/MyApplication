pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    // A linha repositoriesMode.set(...) foi removida para eliminar os avisos @Incubating.
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "SWADEbuilder"
include(":app")
