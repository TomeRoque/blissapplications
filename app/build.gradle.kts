plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.kapt")
    id("dagger.hilt.android.plugin")
}

android {
    namespace = "com.blissapplications.tomeroque"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.blissapplications.tomeroque"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.paging.common.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation (libs.hilt.android)
    kapt(libs.hilt.android.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation (libs.androidx.navigation.compose)

    implementation(libs.okhttp)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.migration)
    testImplementation(libs.androidx.room.testing)
    kapt("androidx.room:room-compiler:2.6.1")

    implementation(libs.coil.compose)

    implementation (libs.androidx.paging.compose)
    implementation (libs.androidx.foundation)
    implementation (libs.material3)
    implementation (libs.androidx.paging.runtime)
}

kapt {
    correctErrorTypes = true
}