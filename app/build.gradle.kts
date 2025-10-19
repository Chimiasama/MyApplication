plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // plugin de serialization
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20"
}

// Exclui globalmente a versão da JetBrains IntelliJ Annotations duplicada
configurations
    .matching { it.name != "kotlinCompilerClasspath" } // opcional: evita excluir do classpath do compilador
    .all {
        exclude(group = "com.intellij", module = "annotations")
    }

android {
    namespace = "com.example.myapplication"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 25
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.get()
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }
}

dependencies {
    // 1) BOM do Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.animation)

    // 2) UI & Material3
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.core.splashscreen)

    // 3) Ícones do Material
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended.v178)

    // 4) Activity Compose, Proto e Room
    implementation(libs.androidx.activity.compose)
    implementation(libs.protolite.well.known.types)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.room.compiler)
    implementation(libs.engage.core)
    implementation(libs.androidx.navigation.common.android)
    implementation(libs.androidx.navigation.compose.android)

    // 5) Tooling no debug
    debugImplementation(libs.androidx.compose.ui.tooling)

    // 6) Core & Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation (libs.gson)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // 7) Serialization JSON
    implementation(libs.kotlinx.serialization.json)

    // 8) Testes
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core.v351)

    // 9) iText para PDF
    implementation(libs.itextg)
}
