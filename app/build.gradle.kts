import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    kotlin("kapt")
    id ("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}


android {
    namespace = "net.metalbrain.paysmart"
    compileSdk {
        version = release(36)
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "net.metalbrain.paysmart"
        minSdk = 33
        targetSdk = 36
        versionCode = 3
        versionName = "1.0.0"
        testInstrumentationRunner = "net.metalbrain.paysmart.HiltTestRunner"
    }

    signingConfigs {
        create("release") {
            val storeFilePath = project.findProperty("RELEASE_STORE_FILE") as? String
            val storePassword = project.findProperty("RELEASE_STORE_PASSWORD") as? String
            val keyAlias = project.findProperty("RELEASE_KEY_ALIAS") as? String
            val keyPassword = project.findProperty("RELEASE_KEY_PASSWORD") as? String

            if (storeFilePath != null && storePassword != null && keyAlias != null && keyPassword != null) {
                storeFile = file(storeFilePath)
                this.storePassword = storePassword
                this.keyAlias = keyAlias
                this.keyPassword = keyPassword
            } else {
                println("⚠️ Warning: Release signing config values not set! AAB will be unsigned.")
            }
        }
    }

    buildTypes {
        getByName("debug") {
            buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:5001/paysmart-7ee79/europe-west2/api\"")
            buildConfigField("String", "FUNCTION_API_URL", "\"http://10.0.2.2:8080\"")
            buildConfigField("Boolean", "IS_LOCAL", "true")
        }
        getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs.getByName("release")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "API_BASE_URL", "\"https://europe-west2-paysmart-7ee79.cloudfunctions.net/api\"")
            buildConfigField("String", "FUNCTION_API_URL", "\"https://europe-west2-paysmart-7ee79.cloudfunctions.net\"")
            buildConfigField("Boolean", "IS_LOCAL", "false")

        }
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.material.icons.extended)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.coil.compose.v270)
    implementation(libs.exoplayer)
    implementation(libs.exoplayer.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.lottie.compose)
    implementation(libs.hilt.core)
    implementation(libs.hilt.lifecycle.viewmodel.compose)
    implementation(libs.androidx.runner)
    implementation(libs.googleid)
    implementation(libs.play.services.auth)
    testImplementation(libs.hilt.android.testing)
    testImplementation(libs.hilt.android.testing)
    androidTestImplementation(libs.hilt.android.testing)
    kaptTest(libs.hilt.compiler)
    androidTestImplementation(libs.mockk)
    androidTestImplementation(libs.arch.core.testing)
    implementation(libs.navigation.compose)
    implementation(libs.cronet.embedded)
    implementation(libs.firebase.appcheck.playintegrity)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.store)

    // Credential Manager (Google Sign-In, Passkeys, etc.)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)

    // Coroutines support for Play Services (you already have this)
    implementation(libs.kotlin.coroutines.play.services)
    implementation(libs.bcrypt)
    implementation(libs.security.crypto)
    implementation(libs.tink)
    implementation(libs.biometric)
    implementation(libs.androidx.ui)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.ui.unit)

    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockk)

    // Android Instrumented tests
    androidTestImplementation(libs.hilt.android.testing)
    kapt(libs.hilt.compiler)
    kaptAndroidTest(libs.hilt.compiler)
    androidTestAnnotationProcessor(libs.hilt.compiler)
    androidTestImplementation(libs.androidx.junit)
    testImplementation(libs.arch.core.testing)
    androidTestImplementation(libs.mockk.android)


    implementation(libs.firebase.appcheck.debug)
    implementation(libs.firebase.functions)
    implementation(libs.kotlin.coroutines.play.services)
    implementation(libs.libphonenumber)
    implementation(libs.firebase.analytics)
    implementation(libs.androidx.material3)

    implementation(libs.datastore.preferences)
    implementation(libs.transport.runtime)
    implementation(libs.androidx.datastore.preferences.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

}
