import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

val trackedVersionProperties = Properties().apply {
    val versionFile = rootProject.file("version.properties")
    if (versionFile.exists()) {
        versionFile.inputStream().use(::load)
    }
}

val localBuildProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use(::load)
    }
}

fun Project.stringPropertyOrEnv(name: String, defaultValue: String = ""): String =
    providers.gradleProperty(name).orNull?.trim().takeUnless { it.isNullOrEmpty() }
        ?: providers.environmentVariable(name).orNull?.trim()?.takeUnless { it.isNullOrEmpty() }
        ?: trackedVersionProperties.getProperty(name)?.trim()?.takeUnless { it.isEmpty() }
        ?: defaultValue

fun Project.intPropertyOrEnv(name: String, defaultValue: Int): Int =
    stringPropertyOrEnv(name, defaultValue.toString()).toInt()

fun Project.localPropertyOrEnv(name: String, defaultValue: String = ""): String =
    localBuildProperties.getProperty(name)?.trim()?.takeUnless { it.isEmpty() }
        ?: providers.environmentVariable(name).orNull?.trim()?.takeUnless { it.isEmpty() }
        ?: defaultValue

fun Project.localBooleanPropertyOrEnv(name: String, defaultValue: Boolean = false): Boolean =
    localPropertyOrEnv(name, defaultValue.toString()).equals("true", ignoreCase = true)

val versionCodeValue = intPropertyOrEnv("PAYSMART_VERSION_CODE", defaultValue = 1)
val versionMajor = intPropertyOrEnv("PAYSMART_VERSION_MAJOR", defaultValue = 1)
val versionMinor = intPropertyOrEnv("PAYSMART_VERSION_MINOR", defaultValue = 0)
val versionPatch = intPropertyOrEnv("PAYSMART_VERSION_PATCH", defaultValue = 0)
val versionPreRelease = stringPropertyOrEnv("PAYSMART_VERSION_PRERELEASE")
val paySmartSemVer = buildString {
    append("$versionMajor.$versionMinor.$versionPatch")
    if (versionPreRelease.isNotEmpty()) {
        append("-")
        append(versionPreRelease)
    }
}
val localDevEnabled = localBooleanPropertyOrEnv("LOCAL_DEV", defaultValue = false)
val localApiBaseUrl = localPropertyOrEnv(
    name = "LOCAL_API_BASE_URL",
    defaultValue = "http://10.0.2.2:5001/paysmart-7ee79/europe-west2/api"
)
val localFunctionApiUrl = localPropertyOrEnv(
    name = "LOCAL_FUNCTION_API_URL",
    defaultValue = "http://10.0.2.2:5001/paysmart-7ee79/europe-west2"
)
val remoteApiBaseUrl = localPropertyOrEnv(
    name = "REMOTE_API_BASE_URL",
    defaultValue = "https://europe-west2-paysmart-7ee79.cloudfunctions.net/api"
)
val remoteFunctionApiUrl = localPropertyOrEnv(
    name = "REMOTE_FUNCTION_API_URL",
    defaultValue = "https://europe-west2-paysmart-7ee79.cloudfunctions.net"
)
val debugApiBaseUrl = if (localDevEnabled) localApiBaseUrl else remoteApiBaseUrl
val debugFunctionApiUrl = if (localDevEnabled) localFunctionApiUrl else remoteFunctionApiUrl
val stripePublishableFromGradle =
    (project.findProperty("STRIPE_PUBLISHABLE_KEY") as? String).orEmpty()

android {
    namespace = "net.metalbrain.paysmart.core.firebase"
    compileSdk = 36

    defaultConfig {
        minSdk = 33
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildTypes {
        getByName("debug") {
            buildConfigField("String", "API_BASE_URL", "\"$debugApiBaseUrl\"")
            buildConfigField("String", "FUNCTION_API_URL", "\"$debugFunctionApiUrl\"")
            buildConfigField("Boolean", "IS_LOCAL", localDevEnabled.toString())
            buildConfigField("Boolean", "APP_CHECK_ENFORCED", "false")
            buildConfigField("Boolean", "PHONE_PNV_PREVIEW_ENABLED", localDevEnabled.toString())
            buildConfigField("String", "IDENTITY_IMAGE_DETECTION_MODE", "\"on_device\"")
            buildConfigField("Boolean", "IDENTITY_IMAGE_DETECTION_FAIL_OPEN", "true")
            buildConfigField("String", "IDENTITY_DOCUMENT_OCR_MODE", "\"remote_api\"")
            buildConfigField("Boolean", "IDENTITY_DOCUMENT_OCR_FAIL_OPEN", "true")
            buildConfigField("String", "STRIPE_PUBLISHABLE_KEY", "\"$stripePublishableFromGradle\"")
            buildConfigField("String", "APP_VERSION_NAME", "\"$paySmartSemVer\"")
            buildConfigField("int", "APP_VERSION_CODE", versionCodeValue.toString())
            buildConfigField("Boolean", "IS_DEBUG_BUILD", "true")
        }
        getByName("release") {
            buildConfigField("String", "API_BASE_URL", "\"https://europe-west2-paysmart-7ee79.cloudfunctions.net/api\"")
            buildConfigField("String", "FUNCTION_API_URL", "\"https://europe-west2-paysmart-7ee79.cloudfunctions.net\"")
            buildConfigField("Boolean", "IS_LOCAL", "false")
            buildConfigField("Boolean", "APP_CHECK_ENFORCED", "true")
            buildConfigField("Boolean", "PHONE_PNV_PREVIEW_ENABLED", "false")
            buildConfigField("String", "IDENTITY_IMAGE_DETECTION_MODE", "\"on_device\"")
            buildConfigField("Boolean", "IDENTITY_IMAGE_DETECTION_FAIL_OPEN", "true")
            buildConfigField("String", "IDENTITY_DOCUMENT_OCR_MODE", "\"remote_api\"")
            buildConfigField("Boolean", "IDENTITY_DOCUMENT_OCR_FAIL_OPEN", "false")
            buildConfigField("String", "STRIPE_PUBLISHABLE_KEY", "\"$stripePublishableFromGradle\"")
            buildConfigField("String", "APP_VERSION_NAME", "\"$paySmartSemVer\"")
            buildConfigField("int", "APP_VERSION_CODE", versionCodeValue.toString())
            buildConfigField("Boolean", "IS_DEBUG_BUILD", "false")
        }
    }
}

dependencies {
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.junit)
    implementation(project(":core:common"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.hilt.core)
    ksp(libs.hilt.compiler)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.performance)
    implementation(libs.firebase.config)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.appcheck.playintegrity)
    implementation(libs.firebase.appcheck.debug)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
}
