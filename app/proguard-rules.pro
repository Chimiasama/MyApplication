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
