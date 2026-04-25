plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "net.metalbrain.paysmart.data.auth"
    compileSdk = 36

    defaultConfig {
        minSdk = 33
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:models"))
    implementation(project(":core:database"))
    implementation(project(":core:firebase"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.hilt.core)
    ksp(libs.hilt.compiler)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.store)
    implementation(libs.firebase.functions)
    implementation(libs.kotlin.coroutines.play.services)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation(libs.facebook.login)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.junit)
}
