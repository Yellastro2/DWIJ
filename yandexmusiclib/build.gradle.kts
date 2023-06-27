plugins {
    id("com.android.library")
    kotlin("android")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

android {
    namespace = "com.yelldev.yandexmusiclib"
    compileSdk = 33
    defaultConfig {


        minSdk = 26

    }

    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/*"
        }
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}



dependencies {



    implementation ("org.json:json:20230227")
    implementation ("commons-codec:commons-codec:1.15")
    implementation ("com.yandex.android:authsdk:2.5.1")
	implementation("androidx.annotation:annotation-jvm:1.7.1")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.2")


}