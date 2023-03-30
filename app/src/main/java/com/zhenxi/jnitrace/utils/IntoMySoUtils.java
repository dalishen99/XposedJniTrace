package com.zhenxi.jnitrace.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.util.Log;

import com.zhenxi.jnitrace.BuildConfig;


import de.robv.android.xposed.XposedHelpers;

/**
 * @author Zhenxi on 2021/5/17
 */
public class IntoMySoUtils {

    public static final String V8 = "arm64-v8a";
    public static final String V7 = "armeabi-v7a";

    private static final String ARM = "arm";
    private static final String ARM64 = "arm64";

    private static final String lib = BuildConfig.project_name + "Lib";

    /**
     * So的 名字 比如 libLVmp.so
     */
    public static void initMySoForName(Context context,
                                       String name,
                                       ClassLoader soClassLoader,
                                       String mIntoSoPath) {
        try {
            String path = getSoPath(context, name, mIntoSoPath);
            if (path != null) {
                LoadSoForPath(path, soClassLoader);
            } else {
                CLog.e(">>>>>>>>>>>>>  not found into so path -> " + name);
            }
        } catch (Throwable e) {
            CLog.e("initMySo error,start printf " + e.getMessage() + " " + e.getLocalizedMessage());
            Log.getStackTraceString(e);
            StackTraceElement[] stackTrace = e.getStackTrace();
            for (StackTraceElement element : stackTrace) {
                CLog.e(element.toString());
            }
        }
    }


    public static boolean is64bit(String xpMoudleName, Context context) throws Exception {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Process.is64Bit();
        }
        PackageManager pm = context.getPackageManager();
        PackageInfo packageInfo = pm.getPackageInfo(xpMoudleName, 0);
        String nativeLibraryDir = packageInfo.applicationInfo.nativeLibraryDir;

        //如果对方App没有So的话,默认使用64
        return !nativeLibraryDir.startsWith(ARM);
    }


    /**
     * 这块可能有问题,需要区分32和64位
     * 1,需要提前解压
     * 2,返回对的路径
     */
    public static String getSoPath(Context context, String name, String intoSoPath) throws Exception {
        String ret = null;
        PackageInfo packageInfo = null;
        String publicSourceDir = null;
        PackageManager pm = context.getPackageManager();
        try {
            packageInfo = pm.getPackageInfo(BuildConfig.APPLICATION_ID, 0);
        } catch (Throwable e) {
            //很多加壳app会在getPackageInfo 失败,这个时候采用默认的config目录
            CLog.e("getSoPath getPackageInfo so path error ,start append path " + e.getMessage());
            publicSourceDir = intoSoPath;
        }
        if (packageInfo != null) {
            //base apk的路径
            publicSourceDir = packageInfo.applicationInfo.publicSourceDir;
        }
        CLog.e("publicSourceDir path -> " + publicSourceDir);

        String destPath = context.getApplicationInfo().dataDir + "/" + lib;
        //尝试解压
        UnZipUtils.UnZipFolder(publicSourceDir, destPath);
        try {
            ret = destPath + "/lib/" + (is64bit(BuildConfig.APPLICATION_ID, context) ? V8 : V7) + "/" + name;
        } catch (Throwable exception) {
            CLog.e("getSoPath is64bit   error " + exception.getMessage());
        }
        return ret;

    }


    /**
     * 这块有个细节问题
     * 注入时候传入的Classloader问题
     * 这个Classloader标识当前So的Classloader()
     * (So 也是需要Classloader的,用于标识)
     * <p>
     * 情况1:
     * 如果传Null 当前Classloader为系统的Classloader
     * 系统的Classloader没有权限去反射得到当前进程的Class
     * 系统的Class里面没有当前进程的Class
     * <p>
     * 情况2:
     * 如果传当前被Hook进程的Classloader进入的时候会直接挂掉
     * 因为模块的这个类，是Xposed new了一个PathClassloader （是个成员变量）
     * (
     * 具体参考 XposedBridge-》loadModule 方法
     * private static void loadModule(String apk) {
     * log("Loading modules from " + apk);
     * <p>
     * if (!new File(apk).exists()) {
     * log("  File does not exist");
     * return;
     * }
     * //加载Xposed模块的 Classloader
     * ClassLoader mcl = new PathClassLoader(apk, BOOTCLASSLOADER);
     * <p>
     * InputStream is = mcl.getResourceAsStream("assets/xposed_init");
     * if (is == null) {
     * log("assets/xposed_init not found in the APK");
     * return;
     * }
     * .....
     * )
     * 这个PathClassloader 不属于当前进程,所以会find不到当前模块的Class直接挂掉
     * （java.lang.ClassNotFoundException: Didn't find class "com.example.vmp.Hook.LHookConfig"
     * on path: DexPathList[[zip file "/data/user/0/com.chinamworld.main/.cache/classes.jar",
     * zip file "/data/app/com.chinamworld.main-1/base.apk"],
     * nativeLibraryDirectories=[/data/app/com.chinamworld.main-1/lib/arm,
     * /data/app/com.chinamworld.main-1/base.apk!/lib/armeabi-v7a, /system/lib, /vendor/lib]]）
     * <p>
     * 情况3:
     * 直接传入当前模块的Classloader this.getclass.getClassloader
     */
    public static void LoadSoForPath(String path, Object object) {
        try {
            CLog.e("load so path ->  " + path);
            if (Build.VERSION.SDK_INT >= 28) {
                String nativeLoad = (String) XposedHelpers.callMethod(Runtime.getRuntime(), "nativeLoad", path, object);
                CLog.e(nativeLoad == null ? "" : nativeLoad);
            } else {
                String doLoad = (String) XposedHelpers.callMethod(Runtime.getRuntime(), "doLoad", path, object);
                CLog.e(doLoad == null ? "" : doLoad);
            }
            //CLogUtils.e("LoadSoForPahth  注入成功 "+path);
        } catch (Throwable e) {
            CLog.e("load so for path " + e.getMessage());
        }
    }
}
