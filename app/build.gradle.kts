plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    // Plugin de serialization
    id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20"
}

// Exclui globalmente a versão duplicada da JetBrains IntelliJ Annotations
configurations
    .matching { it.name != "kotlinCompilerClasspath" }
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

    // ------------------------------------------------------------
    // 🔹 Product Flavors (versões lite e full)
    // ------------------------------------------------------------
    flavorDimensions += "edition" // se o seu já tiver "version", pode manter o nome

    productFlavors {
        create("lite") {
            dimension = "edition"            // ou "version" se já usa esse nome
            applicationIdSuffix = ".lite"
            versionNameSuffix = "-lite"
            // flags de build para usar no código (BuildConfig.SHOW_LISTA_COMPLETA/ENABLE_LONG_TEXTS)
            buildConfigField("boolean", "SHOW_LISTA_COMPLETA", "false")
            buildConfigField("boolean", "ENABLE_LONG_TEXTS", "false")

            resValue("string", "app_name", "SWADE Criador (Lite)")
            resValue("bool", "show_lista_completa", "false")
            resValue("bool", "enable_long_texts", "false")
        }
        create("full") {
            dimension = "edition"            // ou "version" se já usa esse nome
            applicationIdSuffix = ".full"
            versionNameSuffix = "-full"
            buildConfigField("boolean", "SHOW_LISTA_COMPLETA", "true")
            buildConfigField("boolean", "ENABLE_LONG_TEXTS", "true")
            resValue("string", "app_name", "SWADE Criador (Completo)")
            resValue("bool", "show_lista_completa", "true")
            resValue("bool", "enable_long_texts", "true")
        }
    }


    // ------------------------------------------------------------
    // 🔹 Build Types
    // ------------------------------------------------------------
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // ------------------------------------------------------------
    // 🔹 Compose (mantém o suporte atual)
    // ------------------------------------------------------------
    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.get()
    }

    // 🔹 Configuração Kotlin padrão (compatível com AGP < 8.3)
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
