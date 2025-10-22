plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Plugin de serialization
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20"
}

// 🔹 Exclui globalmente a versão duplicada da JetBrains IntelliJ Annotations
configurations
    .matching { it.name != "kotlinCompilerClasspath" }
    .all {
        exclude(group = "com.intellij", module = "annotations")
    }

android {
    namespace = "com.example.swadebuilder"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.swadebuilder"
        minSdk = 25
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        // 🔹 Gera apenas para arquitetura moderna (reduz tamanho do APK)
        ndk {
            abiFilters += listOf("arm64-v8a")
        }

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    // ------------------------------------------------------------
    // 🔹 Product Flavors (versões Lite e Full)
    // ------------------------------------------------------------
    flavorDimensions += "edition"

    productFlavors {
        create("lite") {
            dimension = "edition"
            applicationIdSuffix = ".lite"
            versionNameSuffix = "-lite"

            // Flags de build (para controle dentro do app)
            buildConfigField("boolean", "SHOW_LISTA_COMPLETA", "false")
            buildConfigField("boolean", "ENABLE_LONG_TEXTS", "false")

            resValue("string", "app_name", "SWADEbuilder")
            resValue("bool", "show_lista_completa", "false")
            resValue("bool", "enable_long_texts", "false")
        }

        create("full") {
            dimension = "edition"
            applicationIdSuffix = ".full"
            versionNameSuffix = "-full"

            buildConfigField("boolean", "SHOW_LISTA_COMPLETA", "true")
            buildConfigField("boolean", "ENABLE_LONG_TEXTS", "true")

            resValue("string", "app_name", "SWADEbuilder (Completo)")
            resValue("bool", "show_lista_completa", "true")
            resValue("bool", "enable_long_texts", "true")
        }
    }

    // ------------------------------------------------------------
    // 🔹 Build Types
    // ------------------------------------------------------------
    buildTypes {
        release {
            // 🔹 Ativa compressão e remoção de código não usado (ProGuard + R8)
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // ------------------------------------------------------------
    // 🔹 Compose
    // ------------------------------------------------------------
    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.get()
    }

    // ------------------------------------------------------------
    // 🔹 Kotlin e Java
    // ------------------------------------------------------------
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-Xcontext-receivers",
            "-opt-in=kotlin.RequiresOptIn"
        )
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17)
    }

    // ------------------------------------------------------------
    // 🔹 Packaging (remove licenças duplicadas)
    // ------------------------------------------------------------
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

// ------------------------------------------------------------
// 🔹 Dependências
// ------------------------------------------------------------
dependencies {
    // --- Jetpack Compose ---
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.animation)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui.tooling.preview)
    debugImplementation(libs.androidx.compose.ui.tooling.v170)

    // --- Material Components ---
    implementation(libs.material)
    implementation(libs.androidx.appcompat.v171)
    implementation(libs.androidx.core.ktx)

    // --- Splash Screen ---
    implementation(libs.androidx.core.splashscreen)

    // --- Ícones do Material ---
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended.v178)

    // --- Navegação e Persistência ---
    implementation(libs.androidx.navigation.common.android)
    implementation(libs.androidx.navigation.compose.android)
    implementation(libs.androidx.room.compiler)
    implementation(libs.protolite.well.known.types)
    implementation(libs.engage.core)

    // --- Ciclo de vida e ViewModel ---
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // --- JSON / Serialização ---
    implementation(libs.gson)
    implementation(libs.kotlinx.serialization.json)

    // --- iText para PDF ---
    implementation(libs.itextg)

    // --- Testes ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core.v351)
}

// ------------------------------------------------------------
// 🔹 Otimizações pós-build
// ------------------------------------------------------------

// Desativa todas as tarefas Lint (acelera build local)
tasks.configureEach {
    if (name.contains("lint", ignoreCase = true)) {
        enabled = false
    }
}

// Loga quando o R8 for chamado (compressão de código)
tasks.withType<com.android.build.gradle.internal.tasks.R8Task>().configureEach {
    doFirst {
        println("⚙️ R8 otimização ativada (full mode)")
    }
}
