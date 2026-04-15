# ── Citra Android ProGuard Rules ──────────────────────────────

# Keep JNI classes
-keep class com.citra.android.jni.** { *; }
-keepclassmembers class com.citra.android.jni.NativeLibrary {
    public static native <methods>;
}

# Keep data models
-keep class com.citra.android.model.** { *; }
-keep class com.citra.android.utils.SettingsSnapshot { *; }

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Compose
-keep class androidx.compose.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# General Android
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,EnclosingMethod
