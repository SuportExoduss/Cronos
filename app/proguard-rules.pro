# ── Stack traces legíveis em produção ─────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Room ──────────────────────────────────────────────────────────────────────
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keepclassmembers @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface *
-keepclassmembers @androidx.room.Dao interface * { *; }

# ── Hilt / Dagger ─────────────────────────────────────────────────────────────
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# ── Kotlin Coroutines ─────────────────────────────────────────────────────────
-keepclassmembers class kotlinx.coroutines.** { *; }
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ── DataStore ─────────────────────────────────────────────────────────────────
-keep class androidx.datastore.** { *; }

# ── Coil ──────────────────────────────────────────────────────────────────────
-keep class coil.** { *; }

# ── Enums do domínio — necessário para valueOf() e name ───────────────────────
-keepclassmembers enum com.exoduss.cronos.domain.model.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    public ** name();
    public ** ordinal();
}

# ── Modelos de dados (data classes com reflexão pelo Hilt/Room) ────────────────
-keep class com.exoduss.cronos.domain.model.** { *; }
-keep class com.exoduss.cronos.data.local.entity.** { *; }