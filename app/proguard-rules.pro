# Add project specific ProGuard rules here.
-keep class com.gdtv.live.** { *; }
-keepattributes *Annotation*
-keepattributes JavascriptInterface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}
