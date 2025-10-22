pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
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
