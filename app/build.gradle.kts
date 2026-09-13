plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.solinone.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.solinone.app"
        minSdk = 24
        targetSdk = 35
        versionCode = 2
        versionName = "0.1.0-alpha.2"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            val keystoreFile = rootProject.file("keystore/solinone-release.jks")
            if (keystoreFile.exists()) {
                storeFile = keystoreFile
                storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "SolinOne@Release2026"
                keyAlias = System.getenv("KEY_ALIAS") ?: "solinone"
                keyPassword = System.getenv("KEY_PASSWORD") ?: "SolinOne@Release2026"
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
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
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:calendar"))
    implementation(project(":core:database"))
    implementation(project(":core:security"))
    implementation(project(":features:finance"))
    implementation(project(":features:calendar"))

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation(platform("androidx.compose:compose-bom:2025.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.8")
    implementation("androidx.biometric:biometric:1.2.0-alpha05")

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
