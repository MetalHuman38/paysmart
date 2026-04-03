plugins {
    alias(libs.plugins.android.library)
}

android {
    namespace = "net.metalbrain.paysmart.core.common"
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.libphonenumber)
    implementation(libs.hilt.core)
}
