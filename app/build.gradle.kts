plugins {
    alias(libs.plugins.androidApplication)
//    id("com.google.gms.google-services") version "4.4.4" apply false

    //id("com.android.application")
   // id("com.google.gms.google-services")
}

android {
    namespace = "com.example.maskan"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.maskan"
        minSdk = 31
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.lottie)
    implementation(libs.cardview)

   // implementation(platform("com.google.firebase:firebase-bom:34.7.0"))
   // implementation("com.google.firebase:firebase-analytics")


}