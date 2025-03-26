plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.harekrishna.otpClasses"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.harekrishna.otpClasses"
        minSdk = 24
        targetSdk = 35
        versionCode = 9 // version 1.1.7 - Bug fixes
        versionName = "1.1.7"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.13"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    applicationVariants.all { variant ->
        variant.outputs.all { output ->
            val versionName = defaultConfig.versionName // Ensure this is correctly defined
            val stringsFile = file("src/main/res/values/strings.xml") // Ensure this points to the correct file

            // Check if the file exists
            if (stringsFile.exists() ) {
                val content = stringsFile.readText() // Read the file's content
                val updatedContent = content.replace(
                    Regex("<string name=\"version_name\">.*?</string>"),
                    "<string name=\"version_name\">$versionName</string>"
                )
                stringsFile.writeText(updatedContent) // Write the updated content back
                true
            } else {
                println("strings.xml not found at ${stringsFile.path}")
                false
            }

        }
    }



}

dependencies {

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))

    // Add the dependency for the Firebase Authentication library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database")

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
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)

    // Material-3
    implementation(libs.androidx.material3)

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
    implementation(kotlin("script-runtime"))
}