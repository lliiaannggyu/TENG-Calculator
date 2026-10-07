# ===== 保留 Kotlin 元数据 =====
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# ===== 保留 ViewModel 构造器（反射用） =====
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}

# ===== 保留 Compose Runtime 相关 =====
-dontwarn androidx.compose.**

# ===== 保留 Wear Compose 相关 =====
-dontwarn androidx.wear.**

# ===== 保留你的数据类 =====
-keep class com.tengwear.jisuanqi.logic.** { *; }
-keep class com.tengwear.jisuanqi.data.** { *; }

# ===== 保留 Kotlin 协程 =====
-dontwarn kotlinx.coroutines.**