# GastoClaro ProGuard / R8 rules

# Keep SQLCipher native symbols
-keep class net.sqlcipher.** { *; }
-keep class net.zetetic.** { *; }

# Keep Room entities and generated code
-keep class com.gastoclaro.data.local.entity.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
