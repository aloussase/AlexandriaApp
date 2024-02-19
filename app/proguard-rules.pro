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

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep class android.app.Person
-keep class android.app.RemoteInput
-keep class android.content.res.Resources
-keep class android.content.res.Resources$Theme
-keep class android.graphics.Bitmap
-keep class android.graphics.Bitmap$Config
-keep class android.graphics.BitmapFactory
-keep class android.graphics.BitmapFactory$Options
-keep class android.graphics.drawable.Icon
-keep class android.graphics.drawable.RippleDrawable
-keep class android.icu.text.DecimalFormatSymbols
-keep class android.os.StrictMode
-keep class android.os.StrictMode$ThreadPolicy
-keep class android.os.StrictMode$ThreadPolicy$Builder
-keep class android.text.PrecomputedText
-keep class android.text.SpannableStringBuilder
-keep class android.widget.Button
-keep class android.widget.TextView
-keep class androidx.appcompat.widget.AppCompatButton
-keep class com.bumptech.glide.load.resource.bitmap.Downsampler