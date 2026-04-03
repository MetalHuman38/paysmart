import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.firebase.crashlytics)
    // androidx.room plugin moved to :core:database
    id("com.google.gms.google-services")
}

val trackedVersionProperties = Properties().apply {
    val versionFile = rootProject.file("version.properties")
    if (versionFile.exists()) {
        versionFile.inputStream().use(::load)
    }
}

val repoGradleProperties = Properties().apply {
    val gradlePropertiesFile = rootProject.file("gradle.properties")
    if (gradlePropertiesFile.exists()) {
        gradlePropertiesFile.inputStream().use(::load)
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
        ?: providers.environmentVariable(name).orNull?.trim().takeUnless { it.isNullOrEmpty() }
        ?: trackedVersionProperties.getProperty(name)?.trim()?.takeUnless { it.isEmpty() }
        ?: defaultValue

fun Project.intPropertyOrEnv(name: String, defaultValue: Int): Int =
    stringPropertyOrEnv(name, defaultValue.toString()).toInt()

fun Project.repoPropertyOrEnv(name: String, defaultValue: String = ""): String =
    repoGradleProperties.getProperty(name)?.trim()?.takeUnless { it.isEmpty() }
        ?: providers.environmentVariable(name).orNull?.trim()?.takeUnless { it.isEmpty() }
        ?: defaultValue

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
val releaseStoreFilePath = localPropertyOrEnv("RELEASE_STORE_FILE")
val releaseStorePasswordValue = localPropertyOrEnv("RELEASE_STORE_PASSWORD")
val releaseKeyAliasValue = localPropertyOrEnv("RELEASE_KEY_ALIAS")
val releaseKeyPasswordValue = localPropertyOrEnv("RELEASE_KEY_PASSWORD")
val releaseStoreFileValue = releaseStoreFilePath
    .takeIf { it.isNotEmpty() }
    ?.let { rootProject.file(it) }
val hasValidReleaseSigning = releaseStoreFileValue?.exists() == true &&
    releaseStorePasswordValue.isNotEmpty() &&
    releaseKeyAliasValue.isNotEmpty() &&
    releaseKeyPasswordValue.isNotEmpty()
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

android {
    namespace = "net.metalbrain.paysmart"
    compileSdk = 36
    ndkVersion = "29.0.14206865"
    val stripePublishableFromGradle =
        (project.findProperty("STRIPE_PUBLISHABLE_KEY") as? String).orEmpty()


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    
    bundle {
        language {
            @Suppress("UnstableApiUsage")
            enableSplit = false
        }
    }

    buildFeatures {
        buildConfig = true
    }

    sourceSets {
        getByName("androidTest") {
            assets.directories.add(file("$rootDir/core/database/schemas").toString())
        }
    }

    defaultConfig {
        applicationId = "net.metalbrain.paysmart"
        minSdk = 33
        targetSdk = 36
        versionCode = versionCodeValue
        versionName = paySmartSemVer
        testInstrumentationRunner = "net.metalbrain.paysmart.HiltTestRunner"

        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))
        }

        // Room schema/KSP args moved to :core:database

        val mapsApiKey = localPropertyOrEnv("MAPS_API_KEY")
            .ifEmpty { localPropertyOrEnv("ADDRESS_VALIDATION_API_KEY") }
        manifestPlaceholders["googleMapsApiKey"] = mapsApiKey
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    signingConfigs {
        create("release") {
            if (hasValidReleaseSigning) {
                storeFile = releaseStoreFileValue
                storePassword = releaseStorePasswordValue
                keyAlias = releaseKeyAliasValue
                keyPassword = releaseKeyPasswordValue
            } else {
                println("⚠️ Warning: Release signing config values are missing or invalid in this repo. Release artifacts will be unsigned.")
            }
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            isShrinkResources = false
            signingConfig = signingConfigs.getByName("debug")
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
            manifestPlaceholders["networkSecurityConfig"] = "@xml/network_security_config_debug"
            manifestPlaceholders["firebasePerformanceCollectionEnabled"] = "false"
        }
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            if (hasValidReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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
            manifestPlaceholders["networkSecurityConfig"] = "@xml/network_security_config_release"
            manifestPlaceholders["firebasePerformanceCollectionEnabled"] = "true"

        }
    }

    buildFeatures {
        compose = true
    }
}

val crashlyticsMappingUploadEnabled: Boolean = providers
    .gradleProperty("crashlyticsUploadEnabled")
    .map { it.equals("true", ignoreCase = true) }
    .orElse(
        providers
            .environmentVariable("CI")
            .map { it.equals("true", ignoreCase = true) }
    )
    .orElse(false)
    .get()

tasks.matching { it.name == "uploadCrashlyticsMappingFileRelease" }.configureEach {
    enabled = crashlyticsMappingUploadEnabled
}


// room {} block moved to :core:database


dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:navigation"))
    implementation(project(":core:ui"))
    implementation(project(":core:security"))
    implementation(project(":core:database"))
    implementation(project(":core:firebase"))
    implementation(project(":data:auth"))
    implementation(project(":data:user"))
    implementation(project(":data:wallet"))
    implementation(project(":data:invoice"))
    implementation(project(":feature:account"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:home"))
    implementation(project(":feature:wallet"))
    implementation(project(":core:invoice-models"))
    implementation(project(":core:models"))
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
    implementation(libs.googleid)
    implementation(libs.google.play.services.maps)
    implementation(libs.stripe.android)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Hilt
    implementation(libs.hilt.core)
    implementation(libs.play.services.analytics)
    implementation(libs.google.play.integrity)
    implementation(libs.google.play.app.update)
    implementation(libs.google.play.app.update.ktx)
    implementation(libs.paging.compose)
    implementation(libs.androidx.runtime)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.lifecycle.viewmodel.compose)

    // Hilt Testing
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)

    androidTestImplementation(libs.mockk)
    androidTestImplementation(libs.arch.core.testing)
    implementation(libs.navigation.compose)
    implementation(libs.cronet.embedded)

    implementation(libs.firebase.appcheck.playintegrity)
    implementation(libs.firebase.appcheck.debug)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.config)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.store)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.performance)

    // Facebook
    implementation(libs.facebook.login)
    implementation(libs.facebook.core)


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

    // Ksp — Room compiler lives in :core:database

    // Android Instrumented tests
    testImplementation(libs.arch.core.testing)
    androidTestImplementation(libs.mockk.android)


    implementation(libs.firebase.functions)
    implementation(libs.libphonenumber)
    implementation(libs.androidx.material3)

    // Room implementation lives in :core:database
    implementation(libs.androidx.sqlite.core)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.paging.runtime)
    testImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.room.testing)


    implementation(libs.datastore.preferences)
    implementation(libs.transport.runtime)
    implementation(libs.androidx.datastore.preferences.core)

    
    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
}
