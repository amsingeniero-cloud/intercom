plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.tvcostabrava.intercom"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.tvcostabrava.intercom"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // IP o dominio del servidor de senializacion (Render/Fly). Editar antes de compilar.
        buildConfigField("String", "SIGNALING_URL", "\"wss://TU-SERVIDOR.onrender.com\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")

    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    implementation(composeBom)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")

    // WebRTC (fork mantenido de GetStream, mismo motor que Google WebRTC)
    implementation("io.getstream:stream-webrtc-android:1.1.1")

    // Cliente WebSocket para senializacion
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
