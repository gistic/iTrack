# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/nabilnoaman/android-sdks/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}



#-dontskipnonpubliclibraryclasses
#-dontobfuscate
#-forceprocessing
#-optimizationpasses 5

#-keep class * extends android.app.Activity
#-assumenosideeffects class android.util.Log {
#   public static *** d(...);
#    public static *** v(...);
#    public static *** i(...);
#    public static *** e(...);
#}


-dontwarn com.parse.**
-keep class com.parse.** { *; }
-keepattributes Signature

# For using GSON @Expose annotation
-keepattributes *Annotation*

# Gson specific classes
-keep class sun.misc.Unsafe { *; }
-keep class com.navibees.sdk.model.metadata.json.** { *; }
-keep class com.navibees.sdk.activity.** { protected *; }
-keep class com.navibees.sdk.model.postioning.MonitoringRegionsReceiver { protected *; }
-keep class com.navibees.sdk.NaviBeesApplication { protected *; public void setAppInForeground(java.lang.Boolean);}
-keep class com.navibees.sdk.activity.ActivitiesActivity { public void customiseActivityInfoIcon(android.widget.ImageView); }
-keep class com.navibees.sdk.CustomPushBroadcastReceiver { protected *; public *;}




#-keep class com.crashlytics.** { *; }
#-keep class com.crashlytics.android.**
-keepattributes SourceFile,LineNumberTable


-keep public class java.lang.Exception
-keep public class * extends java.lang.Exception

#Fresco Start
# Keep our interfaces so they can be used by other ProGuard rules.
# See http://sourceforge.net/p/proguard/bugs/466/
-keep,allowobfuscation @interface com.facebook.common.internal.DoNotStrip

# Do not strip any method/class that is annotated with @DoNotStrip
-keep @com.facebook.common.internal.DoNotStrip class *
-keepclassmembers class * {
    @com.facebook.common.internal.DoNotStrip *;
}

# Keep native methods
-keepclassmembers class * {
    native <methods>;
}

-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn com.android.volley.toolbox.**
#Fresco End
