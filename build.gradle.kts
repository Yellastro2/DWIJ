plugins {
    //trick: for the same plugin versions in all sub-modules
    id("com.android.application").version("8.0.1").apply(false)
    id("com.android.library").version("8.0.1").apply(false)
    kotlin("android").version("1.8.10").apply(false)
    kotlin("multiplatform").version("1.8.10").apply(false)
//    kotlin("kapt") version "1.9.0"
    id("com.google.devtools.ksp") version "1.9.0-1.0.11"
}
buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.9.0"))
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
