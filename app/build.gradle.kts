plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val material3Version: String = providers.gradleProperty("material3").get()

android {
    namespace = "dev.dileepapeiris.sheetpageroverscroll"
    compileSdk = 37
    defaultConfig {
        applicationId = "dev.dileepapeiris.sheetpageroverscroll"
        minSdk = 31
        targetSdk = 37
        versionCode = 1
        versionName = material3Version
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "MATERIAL3_VERSION", "\"$material3Version\"")
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation("androidx.compose.material3:material3:$material3Version")
    // Pinned so only Material3 changes between builds.
    implementation("androidx.compose.foundation:foundation:1.12.1")
    implementation("androidx.activity:activity-compose:1.10.1")

    debugImplementation("androidx.compose.ui:ui-test-manifest:1.12.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.12.1")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")
}
