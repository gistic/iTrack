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


#-dontwarn retrofit.**
#-keep class retrofit.** { *; }
-keepattributes Signature
-keepattributes Exceptions


# Keep our interfaces so they can be used by other ProGuard rules.
# See http://sourceforge.net/p/proguard/bugs/466/

#Parse
-dontwarn com.parse.**

#Fresco
# Keep native methods
-keepclassmembers class * {
    native <methods>;
}

-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn com.android.volley.toolbox.**


##Jackson
-dontwarn jcifs.http.**
-dontwarn org.codehaus.jackson.map.ext.**
-keepattributes *Annotation*,EnclosingMethod
-keepnames class org.codehaus.jackson.** { *; }

#Esri
-dontwarn com.esri.core.internal.**
-keepnames class com.esri.** { *; }

-keep class com.crashlytics.** { *; }
-keep class com.crashlytics.android.**
-keepattributes SourceFile,LineNumberTable

