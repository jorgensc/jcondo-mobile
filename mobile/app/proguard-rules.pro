# Os modelos da API sao preenchidos por reflexao pelo Gson, entao os nomes
# dos campos nao podem ser renomeados pelo R8 numa build de release.
-keep class com.jcondo.mobile.api.model.** { *; }

# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keep,allowobfuscation interface retrofit2.Call
-keep,allowobfuscation class retrofit2.Response
