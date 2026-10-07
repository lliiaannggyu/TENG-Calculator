plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.tengwear.jisuanqi"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.tengwear.jisuanqi"
        minSdk = 25
        targetSdk = 35
        versionCode = 2
        versionName = "2.0"

        // ★ 只保留中英文资源，删掉依赖库带进来的其他语言资源
        resourceConfigurations += listOf("zh", "en")
    }

    buildTypes {
        release {
            // ★ 代码压缩 + 混淆
            isMinifyEnabled = true
            // ★ 资源压缩（删除未引用资源）
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    // ===== SplashScreen 兜底（避免模板残留引用报错） =====
    implementation("androidx.core:core-splashscreen:1.0.1")

    // ===== Wear OS Compose Material 3 (Material 3 Expressive) =====
    implementation("androidx.wear.compose:compose-material3:1.5.0")
    implementation("androidx.wear.compose:compose-foundation:1.5.0")

    // ===== Compose 基础库 =====
    implementation("androidx.compose.ui:ui:1.7.6")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.6")
    implementation("androidx.compose.runtime:runtime:1.7.6")
    implementation("androidx.compose.foundation:foundation:1.7.6")

    // ===== Activity / Lifecycle =====
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // ===== 核心 KTX =====
    implementation("androidx.core:core-ktx:1.15.0")

    // ===== Wear 基础库 =====
    implementation("androidx.wear:wear:1.3.0")

    // ===== 调试工具 =====
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.6")
    debugImplementation("androidx.wear.compose:compose-ui-tooling:1.5.0")
}