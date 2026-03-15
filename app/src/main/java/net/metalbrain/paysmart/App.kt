package net.metalbrain.paysmart

import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.HiltAndroidApp
import net.metalbrain.paysmart.core.service.update.FirebaseRemoteConfigUpdatePolicyConfigProvider

@HiltAndroidApp
class App : BaseApp() {
    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        val crashlytics = FirebaseCrashlytics.getInstance()
        val firebasePerformance = FirebasePerformance.getInstance()
        val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val crashlyticsEnabled = !BuildConfig.IS_LOCAL
        val performanceEnabled = !BuildConfig.IS_LOCAL && !BuildConfig.DEBUG
        crashlytics.isCrashlyticsCollectionEnabled = crashlyticsEnabled
        firebasePerformance.isPerformanceCollectionEnabled = performanceEnabled
        crashlytics.setCustomKey("build_type", BuildConfig.BUILD_TYPE)
        crashlytics.setCustomKey("is_local", BuildConfig.IS_LOCAL)
        if (crashlyticsEnabled) {
            crashlytics.sendUnsentReports()
        }

        if (BuildConfig.DEBUG) {
            firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance(),
            )
        } else {
            firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance(),
            )
        }

        if (BuildConfig.IS_LOCAL) {
            FirebaseAuth.getInstance().useEmulator("10.0.2.2", 9099)
            FirebaseFirestore.getInstance().useEmulator("10.0.2.2", 8082)
            FirebaseFunctions.getInstance().useEmulator("10.0.2.2", 5001)
            FirebaseStorage.getInstance().useEmulator("10.0.2.2", 9199)
        }

        val remoteConfigSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else 21_600)
            .build()
        runCatching {
            firebaseRemoteConfig.setConfigSettingsAsync(remoteConfigSettings)
            firebaseRemoteConfig.setDefaultsAsync(
                FirebaseRemoteConfigUpdatePolicyConfigProvider.DEFAULT_VALUES
            )
            firebaseRemoteConfig.fetchAndActivate()
        }

        // 🔄 Configure Firestore Offline Caching
        val firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(
                PersistentCacheSettings.newBuilder()
                    .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                    .build()
            )
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = firestoreSettings
    }
}
