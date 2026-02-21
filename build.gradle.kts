// Top-level build file

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.dotenv) apply false
    alias(libs.plugins.hilt) apply false
    id("androidx.room") version "2.8.4" apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
}
