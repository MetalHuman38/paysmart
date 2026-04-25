plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "net.metalbrain.paysmart.feature.account"
    compileSdk = 36

    defaultConfig {
        minSdk = 33
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:database"))
    implementation(project(":core:navigation"))
    implementation(libs.navigation.compose)
    implementation(libs.google.play.services.maps)
    implementation(project(":core:firebase"))
    implementation(project(":core:models"))
    implementation(project(":core:security"))
    implementation(project(":core:ui"))
    implementation(project(":data:auth"))
    implementation(project(":data:user"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.material3)
    implementation(libs.material.icons.extended)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.foundation)
    implementation(libs.lottie.compose)
    implementation(libs.hilt.core)
    implementation(libs.hilt.lifecycle.viewmodel.compose)
    ksp(libs.hilt.compiler)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.store)
    implementation(libs.kotlin.coroutines.play.services)
    implementation(libs.datastore.preferences)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.biometric)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.libphonenumber)
    implementation(libs.gson)
    implementation(libs.okhttp)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.junit)
}
