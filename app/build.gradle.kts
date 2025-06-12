plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("io.realm.kotlin")
    // Considera el plugin kotlin-kapt si usas procesadores de anotaciones para Java
    // que no son compatibles con KSP (aunque Glide ya soporta KSP, así que kapt podría no ser necesario solo para Glide).
    // Si solo usas KSP (como para Glide), este no es necesario.
    // id("org.jetbrains.kotlin.kapt") // Si usas annotationProcessor para librerías Java que lo requieran
    id("com.google.devtools.ksp") version "1.9.22-1.0.16"
}

android {
    namespace = "com.example.bimu" // Cambia a tu namespace real
    compileSdk = 34 // Usa la última API estable como compileSdk

    defaultConfig {
        applicationId = "com.example.bimu" // Cambia a tu applicationId real
        minSdk = 26
        targetSdk = 34 // Mantén targetSdk alineado con una API estable y probada
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true // Habilita para producción
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Considera añadir reglas específicas de ProGuard para Realm si es necesario,
            // aunque el plugin de Realm suele manejarlas.
            // Ver documentación de Realm sobre ProGuard/R8.
        }
        debug {
            isMinifyEnabled = false
            // Puedes añadir applicationIdSuffix ".debug" para instalar debug y release juntas
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        // La toolchain se encargará de la versión del JDK para la compilación,
        // pero estas opciones son para la compatibilidad del bytecode.
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // No necesitas kotlinOptions { jvmTarget = "17" } aquí si usas jvmToolchain(17) abajo
    // kotlinOptions {
    //    jvmTarget = "17"
    // }

    buildFeatures {
        viewBinding = true // Si usas ViewBinding junto con Compose o en partes XML
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            // Realm puede añadir archivos en META-INF/native-image/... que a veces necesitan excluirse
            // si hay conflictos de duplicados, aunque no es común con las últimas versiones.
            // excludes += "META-INF/native-image/**"
        }
    }
}

// Configuración de la toolchain para Kotlin a nivel de módulo
kotlin {
    jvmToolchain(17) // Asegura que Kotlin compile con JDK 17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
    }
}

// Y TAMBIÉN ESTO PARA KSP (aunque KSP usa KotlinCompile, seamos explícitos):
tasks.withType<com.google.devtools.ksp.gradle.KspTask>().configureEach {
    // No hay una forma directa de establecer jvmTarget en KspTask,
    // pero asegurémonos de que el JDK que usa KSP se alinee.
    // El plugin KSP debería respetar el jvmTarget de la tarea KotlinCompile asociada.
    // Sin embargo, si el problema persiste, podemos intentar influir en el JDK que KSP usa,
    // aunque esto es menos directo.
    // Por ahora, la configuración anterior de KotlinCompile debería ser la principal.
}

dependencies {
    // Kotlin
    implementation("androidx.core:core-ktx:1.12.0") // O la última estable
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0") // O la última estable
    implementation("androidx.activity:activity-ktx:1.9.0") // O la última estable

    // UI - Material Design y AppCompat (si usas vistas XML además de Compose)
    implementation("com.google.android.material:material:1.11.0") // O la última estable
    implementation("androidx.appcompat:appcompat:1.6.1") // O la última estable
    implementation("androidx.constraintlayout:constraintlayout:2.1.4") // Si usas ConstraintLayout en XML


    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation ("com.squareup.okhttp3:okhttp:4.11.0")
    implementation ("com.squareup.moshi:moshi:1.14.0")

    // Realm Kotlin SDK
    // Usa la misma versión que el plugin de Gradle
    // Realm Kotlin SDK
//    implementation("io.realm.kotlin:library-base:1.13.0")
    // Si usas sincronización con MongoDB Atlas (lo normal), añade:
//    implementation("io.realm.kotlin:library-sync:1.13.0")
    // implementation("com.google.firebase:protolite-well-known-types:18.0.1") // Generalmente no es necesaria con versiones recientes de AGP y Realm. Añadir solo si hay errores específicos de tipos de Protobuf.

    // Navegación (si usas Jetpack Navigation)
    val navVersion = "2.7.7" // O la última estable
    implementation("androidx.navigation:navigation-fragment-ktx:$navVersion") // Para fragmentos
    implementation("androidx.navigation:navigation-ui-ktx:$navVersion")      // Para UI con vistas XML

    // ViewModel y LiveData (si los usas, aunque con Compose se prefiere State)
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")

    // OpenStreetMap
    implementation("org.osmdroid:osmdroid-android:6.1.18") // O la última estable

    // Carga de Imágenes (Glide)
    val glideVersion = "4.16.0" // O la última estable
    implementation("com.github.bumptech.glide:glide:$glideVersion")
    // Usa KSP en lugar de kapt para Glide si es posible (Glide 4.13.0+ soporta KSP)
    ksp("com.github.bumptech.glide:ksp:$glideVersion") // O compiler si usas kapt: annotationProcessor("com.github.bumptech.glide:compiler:$glideVersion")

    // Pruebas
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    // Considera quitar dependencias no usadas, como:
    // implementation("androidx.wear.compose:compose-material:1.4.1") // Si no es para Wear OS
    // implementation("androidx.annotation:annotation:1.6.0") // A menudo es una dependencia transitiva
}

configurations.all {
    resolutionStrategy.eachDependency {
        if(requested.group == "org.jetbrains.kotlin") {
            useVersion("1.9.23")
        }
    }
}