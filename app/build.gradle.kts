plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)

        id("kotlin-kapt") // Apply the kapt plugin directly
    }
android {
    namespace = "com.najah.qurantest"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.najah.qurantest"
        minSdk = 24
        targetSdk = 35
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation (libs.androidx.room.runtime) // Room Runtime
    annotationProcessor (libs.androidx.room.compiler) // Room Compiler for generating code

    implementation(libs.kotlinx.coroutines.android)
    implementation (libs.androidx.lifecycle.viewmodel.ktx )
    // For Kotlin support
    implementation (libs.androidx.room.ktx) // Kotlin extensions for Room
    kapt (libs.androidx.room.compiler) // For Kotlin
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}