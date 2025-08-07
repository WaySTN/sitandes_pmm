// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.2.2")
        classpath("com.google.gms:google-services:4.4.3") // Update ke versi terbaru
        // NOTE: Do not place your application dependencies here
    }
}

plugins {
    id("com.android.application") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.gms.google-services") version "4.4.3" apply false // Update ke versi terbaru
}

allprojects {
    configurations.all {
        resolutionStrategy {
            force("com.google.firebase:firebase-common:20.3.0")
        }
    }
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory)
}