plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.devtools.ksp") version "1.9.0-1.0.11"
}



android {
    namespace = "com.yelldev.dwij.android"
    compileSdk = 33
    defaultConfig {
        applicationId = "com.yelldev.dwij.android"
        minSdk = 26
        targetSdk = 29
        versionCode = 5
        versionName = "0.0.10_wave_release_2"
        manifestPlaceholders["YANDEX_CLIENT_ID"] = "23cabbbdc6cd418abb4b39c32c41195d"
    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/*"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

}

dependencies {
    implementation(project(":shared"))
	implementation("com.google.android.gms:play-services-base:18.2.0")
	implementation("com.google.android.gms:play-services-auth:20.7.0")

    // Navigation Components
    val navVersion = "2.6.0"
    implementation ("androidx.navigation:navigation-fragment-ktx:$navVersion")
    implementation ("androidx.navigation:navigation-ui-ktx:$navVersion")

    implementation("com.google.android.material:material:1.3.0")
    implementation("com.google.ar:core:1.30.0")
    implementation("androidx.core:core-ktx:1.10.1")
    implementation ("androidx.media:media:1.2.0")

    implementation(project(mapOf("path" to ":yandexmusiclib")))
    implementation("androidx.fragment:fragment:1.4.1")

    implementation("com.google.android.flexbox:flexbox:3.0.0")

    val room_version = "2.5.2"

    implementation("androidx.room:room-runtime:$room_version")
    annotationProcessor("androidx.room:room-compiler:$room_version")

    // To use Kotlin annotation processing tool (kapt)
//    kapt("androidx.room:room-compiler:$room_version")
    // To use Kotlin Symbol Processing (KSP)
    ksp("androidx.room:room-compiler:$room_version")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")

    implementation("io.reactivex.rxjava3:rxandroid:3.0.2")
    // Because RxAndroid releases are few and far between, it is recommended you also
    // explicitly depend on RxJava's latest version for bug fixes and new features.
    // (see https://github.com/ReactiveX/RxJava/releases for latest 3.x.x version)
    implementation("io.reactivex.rxjava3:rxjava:3.1.5")

}