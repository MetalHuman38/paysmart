


-dontwarn net.metalbrain.paysmart.Hilt_App
-dontwarn net.metalbrain.paysmart.Hilt_MainActivity
-dontwarn dagger.hilt.android.internal.**

-keepnames class net.metalbrain.paysmart.App
-keepnames class net.metalbrain.paysmart.MainActivity

-keep class dagger.hilt.android.internal.** { *; }
-keep class * extends dagger.hilt.internal.aggregatedroot.AggregatedRoot { *; }
-keep class * extends dagger.hilt.internal.processedrootsentinel.ProcessedRootSentinel { *; }
-keep class dagger.hilt.internal.aggregatedroot.codegen.** { *; }
-keep class dagger.hilt.internal.processedrootsentinel.codegen.** { *; }
-keep class hilt_aggregated_deps.** { *; }

-keep class * extends dagger.hilt.android.internal.managers.ActivityComponentManager { *; }
-keep class * extends dagger.hilt.android.internal.managers.FragmentComponentManager { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager { *; }
-keep class * extends dagger.hilt.android.internal.managers.ServiceComponentManager { *; }
-keep class * extends dagger.hilt.android.internal.managers.BroadcastReceiverComponentManager { *; }

-keepclasseswithmembers class * {
    @dagger.hilt.android.AndroidEntryPoint *;
}

-keepclasseswithmembers class * {
    @dagger.hilt.EntryPoint *;
}

-keepclasseswithmembers class * {
    @dagger.assisted.Assisted *;
}

-keepclasseswithmembers class * {
    @dagger.assisted.AssistedFactory *;
}

-keepclasseswithmembers class * {
    @dagger.assisted.AssistedInject *;
}

# Keep Application and Activity classes that are Hilt entry points
# 🔥 CRITICAL: Keep Application class with its ORIGINAL NAME
-keepclassmembers class net.metalbrain.paysmart.App {
    <init>();
}
-keep class net.metalbrain.paysmart.App { public protected *; }
-keepnames class net.metalbrain.paysmart.App
-keep class net.metalbrain.paysmart.MainActivity { *; }

# Keep Hilt's generated classes (as a fallback)
-keepnames class **.Hilt_* { *; }
# --- End Hilt ---


-keep class androidx.hilt.navigation.** { *; }


# If you're using ViewModels or Workers with Hilt
-keep class androidx.hilt.** { *; }
-keep class * extends androidx.hilt.** { *; }

-keepclassmembers class net.metalbrain.paysmart.domain.model.SecuritySettingsModel {
    public <init>();
    public *;
}

-keep class net.metalbrain.paysmart.domain.model.SecuritySettingsModel { *; }
-keep class net.metalbrain.paysmart.domain.model.UserStatus { *; }

-keep class net.metalbrain.paysmart.core.locale.** { *; }
-keep class net.metalbrain.paysmart.domain.model.** { *; }
-keep class net.metalbrain.paysmart.domain.model.Language { *; }
-keep class net.metalbrain.paysmart.domain.model.Country { *; }
-keep class net.metalbrain.paysmart.domain.model.AuthUserModel { *; }
-keep class net.metalbrain.paysmart.domain.model.SecuritySettingsModelKt { *; }
-keepattributes Signature
-keepattributes *Annotation*
-keep class kotlin.Metadata { *; }

# Keep all resources in R.string and R.array (prevent R8 stripping)
-keepclassmembers class **.R$* {
    public static <fields>;
}

# If you're loading localized content dynamically by name
-keepclassmembers class net.metalbrain.paysmart.R$string {
    public static <fields>;
}


# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
