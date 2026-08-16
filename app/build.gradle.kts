plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.lucas.habitos"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.lucas.habitos"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "2.1"
    }

    /*
     * Firma fija para las compilaciones de debug.
     *
     * Sin esto, GitHub Actions crea un debug.keystore nuevo en cada compilación,
     * la firma cambia, y Android se niega a instalar el APK encima del anterior
     * ("App no instalada"). Con un keystore versionado la firma es siempre la
     * misma y las actualizaciones se instalan sin desinstalar ni perder datos.
     *
     * No es un secreto: "android" es la contraseña que usa Android por defecto y
     * este keystore solo sirve para depurar. Para publicar en Play hay que crear
     * otro y guardarlo en GitHub Secrets, nunca en el repositorio.
     */
    signingConfigs {
        getByName("debug") {
            storeFile = rootProject.file("habitos-debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
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

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
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

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.material3:material3")

    // Corrutinas para la cuenta atrás del servicio de enfoque. Llegan de forma
    // transitiva con lifecycle, pero se declaran a la vista para que nadie las
    // quite por error al tocar dependencias.
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}
