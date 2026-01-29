# --- Corrige erros de R8 sobre classes ausentes ---
-dontwarn java.sql.**
-dontwarn javax.lang.model.**
-dontwarn com.google.auto.common.**
-dontwarn com.squareup.javapoet.**
-dontwarn org.sqlite.**

# --- Kotlinx Serialization ---
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
    @kotlinx.serialization.SerialName <methods>;
}
-keepnames class kotlinx.serialization.json.** { *; }

# --- Security Hardening ---
# Repackage all classes to a single package (flattens structure)
-repackageclasses 'x.y.z'
-flattenpackagehierarchy 'x.y.z'

# Allow more aggressive optimizations
-allowaccessmodification

# Remove Log calls in release
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# Keep necessary Android components (Activities, etc. are usually kept by default AAPT rules, but explicit keeps can be safe)
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# Keep serialization data classes
-keep @kotlinx.serialization.Serializable class * { *; }
