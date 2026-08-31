# Keep Room entities to prevent R8 from breaking serialization
-keep @androidx.room.Entity class * { *; }

# Keep tink annotation classes (required by R8/minify)
-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn javax.annotation.concurrent.GuardedBy
-dontwarn javax.annotation.meta.TypeQualifierDefault
