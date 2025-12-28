# Add project specific ProGuard rules here.
# Keep kotlinx serialization classes
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.familyfinance.sheet.**$$serializer { *; }
-keepclassmembers class com.familyfinance.sheet.** {
    *** Companion;
}
-keepclasseswithmembers class com.familyfinance.sheet.** {
    kotlinx.serialization.KSerializer serializer(...);
}
