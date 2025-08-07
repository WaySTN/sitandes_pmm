plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services") // Perbaikan: hapus versi dari sini
}

android {
    namespace = "com.example.sitandes"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.sitandes"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    // AndroidX & UI (Edge-to-Edge)
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0") // Update ke versi terbaru
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Untuk Edge-to-Edge
    implementation("androidx.activity:activity-ktx:1.8.2") // Update ke versi terbaru
    implementation("androidx.window:window:1.2.0") // Update ke versi terbaru

    // Firebase BOM untuk manajemen versi yang konsisten
    implementation(platform("com.google.firebase:firebase-bom:32.7.2"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-database") // Firebase Realtime Database
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-storage")
    implementation("com.google.firebase:firebase-dynamic-links-ktx")

    // Jika Anda ingin versi spesifik untuk Realtime Database (opsional karena sudah ada di BOM)
    implementation("com.google.firebase:firebase-database:22.0.0")

    // Firebase UI
    implementation("com.firebaseui:firebase-ui-firestore:8.0.2")

    // Image Libraries
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6") // Update ke versi terbaru
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")

    // Other UI components
    implementation("com.tbuonomo:dotsindicator:4.3")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation ("com.cloudinary:cloudinary-android:2.3.1")

    implementation ("androidx.recyclerview:recyclerview:1.3.2")
    implementation ("com.google.android.material:material:1.11.0")
    implementation ("androidx.appcompat:appcompat:1.6.1")
}