plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.dagger.hilt)

}

android {
    namespace = "com.harekrishna.otpClasses"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.harekrishna.otpClasses"
        minSdk = 26
        targetSdk = 36
        versionCode = 12 // version 1.1.10 - Add term and condition validation on registration screen on submit..
        versionName = "1.1.10"

        //resValue("string", "version_name", versionName ?: "unknown")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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

//    ksp("com.google.devtools.ksp:symbol-processing-api:2.3.4")
    // EXCEL Processing
    implementation("org.apache.poi:poi-ooxml:5.2.3")

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)
    
    // >>> ADDED HILT COMPOSE RUNTIME <<<
    implementation(libs.androidx.hilt.navigation.compose)


    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:34.11.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")
    implementation("com.google.firebase:firebase-config")
    implementation("com.google.firebase:firebase-analytics")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    implementation("io.jsonwebtoken:jjwt-impl:0.11.5")
    implementation("io.jsonwebtoken:jjwt-jackson:0.11.5")  // Jackson for JSON parsing


    // Credential Manager
    implementation("androidx.credentials:credentials:1.5.0-rc01")

    // optional - needed for credentials support from play services, for devices running
    // Android 13 and below.
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0-rc01")

    // Play Service
    implementation(libs.gms.play.services.auth.v2070)
    implementation(libs.play.services.auth.api.phone)
    implementation(libs.play.services.identity)

    implementation(libs.accompanist.pager)
    implementation(libs.accompanist.pager.indicators)

    // Coil - Image Loading Library
    implementation(libs.coil.compose)

    // Material Icon Extended
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.play.services.auth)
    implementation(libs.googleid) // Or latest version

    // Room
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.runtime)
    implementation(libs.room.ktx)


    // Okhttp and its utilities
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)
    implementation(libs.gson)

    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.places)
    implementation(libs.androidx.media3.common)

    // Data store
    implementation(libs.androidx.datastore.preferences)

    // Kotlin and Compose utilities
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)

    // Material-3
    implementation(platform(libs.androidx.compose.bom))
    implementation("androidx.compose.material3:material3")

    // Room testing
    testImplementation("androidx.room:room-testing:2.6.1")

// Coroutines testing
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

// AndroidX Test (needed for ApplicationProvider)
    testImplementation("androidx.test:core:1.5.0")
    testImplementation("androidx.test.ext:junit:1.1.5")

    // Testing
    testImplementation(libs.mockito.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockwebserver)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}