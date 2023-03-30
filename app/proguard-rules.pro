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


# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile


## 修改包名
-repackageclasses "Zx"
## 忽略访问修饰符，配合上一句使用
-allowaccessmodification

-keep  public class com.example.vmp.Hook.LHook
-keep  public class com.example.vmp.Hook.LHookConfig

#代码优化选项，不加该行会将没有用到的类删除，这里为了验证时间结果而使用，在实际生产环境中可根据实际需要选择是否使用
-dontshrink

-dontwarn android.support.annotation.Keep
#保留注解，如果不添加改行会导致我们的@Keep注解失效
-keepattributes *Annotation*
-keep @android.support.annotation.Keep class *

-keep public class com.example.vmp.MainActivity

-printconfiguration proguard-rules-dump.txt
### 忽略警告
#-ignorewarning
-dontobfuscate

-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

