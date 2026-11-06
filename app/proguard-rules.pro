# Add project specific ProGuard rules here.
-keepattributes Signature
-keepattributes *Annotation*

# Retrofit / Gson models
-keep class com.shasthobondhu.app.data.remote.model.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }
