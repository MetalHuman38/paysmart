# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

-keepclassmembers class net.metalbrain.paysmart.domain.model.SecuritySettings {
    public <init>();
    public *;
}

-keep class net.metalbrain.paysmart.domain.model.SecuritySettings { *; }
-keep class net.metalbrain.paysmart.domain.model.UserStatus { *; }

-keep class net.metalbrain.paysmart.core.locale.** { *; }
-keep class net.metalbrain.paysmart.domain.model.** { *; }
-keep class net.metalbrain.paysmart.domain.model.Language { *; }
-keep class net.metalbrain.paysmart.domain.model.Country { *; }
-keep class net.metalbrain.paysmart.domain.model.AuthUserModel { *; }
-keep class net.metalbrain.paysmart.domain.model.CloudSecurityModelKt { *; }
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
