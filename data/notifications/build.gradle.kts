plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "net.metalbrain.paysmart.data.notifications"
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
    implementation(project(":data:auth"))
    implementation(project(":feature:home"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.hilt.core)
    ksp(libs.hilt.compiler)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.store)
    implementation(libs.firebase.messaging)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.junit)
}
