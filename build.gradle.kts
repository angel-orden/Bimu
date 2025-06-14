// Top-level build file
buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()

    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.4.1") // Mantén esta, AGP está en google()
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.23") // Kotlin plugin está en mavenCentral() o google()
        classpath("io.realm.kotlin:gradle-plugin:1.13.0") // Realm plugin está en gradlePluginPortal()
    }
}