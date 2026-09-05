@file:Suppress("UnstableApiUsage", "DEPRECATION")

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    // Plugin de serialization
    id("org.jetbrains.kotlin.plugin.serialization") version "2.4.10"
}

// 🔹 Exclui globalmente a versão duplicada da JetBrains IntelliJ Annotations
configurations
    .matching { it.name != "kotlinCompilerClasspath" }
    .all {
        exclude(group = "com.intellij", module = "annotations")
    }

android {
    namespace = "com.example.swadebuilder"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.swadebuilder"
        minSdk = 25
        targetSdk = 36
        versionCode = 23
        versionName = "3.1"

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }

        bundle {
            abi {
                enableSplit = false
            }
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
            // Descrições longas agora existem também na Lite (descricaoLite, genéricas e sem
            // reproduzir o texto do livro original) — não há mais motivo para esconder o botão
            // "Ver detalhes" nesta flavor.
            buildConfigField("boolean", "ENABLE_LONG_TEXTS", "true")
            buildConfigField("boolean", "ENABLE_PB_WALLET_REDESIGN", "true")

            resValue("string", "app_name", "SWADEbuilder")
            resValue("bool", "enable_long_texts", "true")
            resValue("bool", "enable_pb_wallet_redesign", "true")
        }

        create("full") {
            dimension = "edition"
            applicationIdSuffix = ".full"
            versionNameSuffix = "-full"

            buildConfigField("boolean", "SHOW_LISTA_COMPLETA", "true")
            buildConfigField("boolean", "ENABLE_LONG_TEXTS", "true")
            buildConfigField("boolean", "ENABLE_PB_WALLET_REDESIGN", "true")

            resValue("string", "app_name", "SWADEbuilder (Completo)")
            resValue("bool", "enable_long_texts", "true")
            resValue("bool", "enable_pb_wallet_redesign", "true")
        }
    }

    // ------------------------------------------------------------
    // 🔹 Build Types
    // ------------------------------------------------------------
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false

            // ✅ CORREÇÃO: Adiciona símbolos de depuração nativos para o Play Console.
            // Nota: Se as bibliotecas de terceiros já estiverem "stripped" (sem símbolos), o aviso pode persistir.
            ndk {
                debugSymbolLevel = "FULL"
            }

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
        resValues = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    // ------------------------------------------------------------
    // 🔹 Packaging (remove licenças duplicadas)
    // ------------------------------------------------------------
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // ------------------------------------------------------------
    // 🔹 Lint (relatório de warnings de código, estilo e sugestões)
    // ------------------------------------------------------------
    lint {
        // Regras de supressão específicas (ex.: falso positivo de biblioteca terceira).
        lintConfig = file("lint.xml")
        // Não quebra o build por causa de warnings — apenas reporta.
        abortOnError = false
        warningsAsErrors = false
        // Roda também nas variantes de release, para não perder avisos.
        checkReleaseBuilds = false
        // Habilita todos os checks de lint (inclusive os desligados por padrão),
        // para refletir o mesmo conjunto de avisos que aparece no Android Studio.
        checkAllWarnings = true
        ignoreWarnings = false
        // Gera relatórios em texto (no próprio console, um por variante — sem
        // caminho fixo, senão a variante "lite" sobrescreveria o texto da "full"),
        // além de HTML e XML para consumo humano e por ferramentas.
        textReport = true
        htmlReport = true
        xmlReport = true
    }
}

// ------------------------------------------------------------
// 🔹 Kotlin e Java
// ✅ CORREÇÃO: Movido para fora do bloco android {}
// ------------------------------------------------------------
kotlin {
    compilerOptions {
        // Agora Kotlin também compila para Java 21
        jvmTarget.set(JvmTarget.JVM_21)
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn"
        )
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
    implementation(libs.androidx.material3)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.foundation)
    androidTestImplementation(libs.androidx.core.testing)
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
    implementation(libs.protolite.well.known.types)
    implementation(libs.engage.core)

    // --- Ciclo de vida e ViewModel ---
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // --- JSON / Serialização ---
    implementation(libs.gson)
    implementation(libs.kotlinx.serialization.json)

    // --- Segurança ---
    implementation(libs.androidx.security.crypto)

    // --- iText para PDF ---
    implementation(libs.itextg)

    // --- Testes ---
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core.v351)
    testImplementation(libs.androidx.core.testing)
}

// ------------------------------------------------------------
// 🔹 Otimizações pós-build
// ------------------------------------------------------------

// Permite desativar lint apenas quando solicitado explicitamente
val disableLint = providers.gradleProperty("disableLint").orNull == "true"
if (disableLint) {
    tasks.configureEach {
        if (name.contains("lint", ignoreCase = true)) {
            enabled = false
        }
    }
}

// Loga quando o R8 for chamado (compressão de código)
tasks.withType<com.android.build.gradle.internal.tasks.R8Task>().configureEach {
    doFirst {
        println("⚙️ R8 otimização ativada (full mode)")
    }
}