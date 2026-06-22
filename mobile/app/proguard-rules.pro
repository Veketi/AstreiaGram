# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Retrofit models
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.aiagram.data.remote.dto.** { *; }
-keep class com.aiagram.domain.model.** { *; }
