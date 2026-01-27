# --- Corrige erros de R8 sobre classes ausentes ---
-dontwarn java.sql.**
-dontwarn javax.lang.model.**
-dontwarn com.google.auto.common.**
-dontwarn com.squareup.javapoet.**
-dontwarn org.sqlite.**

# --- Kotlinx Serialization ---
-keepattributes *Annotation*, InnerClasses, Signature, EnclosingMethod
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
    @kotlinx.serialization.SerialName <methods>;
    @kotlinx.serialization.Serializable <fields>;
    @kotlinx.serialization.Serializable <methods>;
}
-keepnames class kotlinx.serialization.json.** { *; }

# --- Security Hardening & Obfuscation ---
-repackageclasses 'com.example.swadebuilder.a'
-allowaccessmodification
-dontskipnonpubliclibraryclasses
-dontskipnonpubliclibraryclassmembers

# Remove Log calls
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Keep our SecurityHardening but obfuscate its internals if possible
# (Actually R8 will obfuscate it unless we keep it. We don't need to keep it if it's used internally)

# Keep data classes that might be serialized via reflection or GSON/Kotlinx
-keep class com.example.swadebuilder.model.** { *; }
-keep class com.example.swadebuilder.util.CharacterStorage$SaveEntry { *; }

# Keep specific Android components
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class * extends com.android.vending.licensing.ILicensingService
