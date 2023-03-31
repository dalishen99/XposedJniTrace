package com.zhenxi.jnitrace;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.zhenxi.jnitrace.utils.CLog;
import com.zhenxi.jnitrace.utils.RootUtils;
import com.zhenxi.jnitrace.utils.ThreadUtils;
import com.zhenxi.jnitrace.utils.ToastUtils;

import java.lang.reflect.Field;
import java.util.List;

/**
 * Created by Zhenxi on
 * 2019/10/18
 */
public class App extends Application {

    public native void AppSecure(Context context);

    static {
        try {
            System.loadLibrary("secure");
        } catch (Throwable e) {
            CLog.e("load Secure so error  " + e.getMessage());
        }
    }


    public void getRoot() {
        ThreadUtils.runOnNonUIThread(() -> {
            if (RootUtils.upgradeRootPermission(getPackageCodePath())) {
                ToastUtils.showToast(getApplicationContext(), "获取root权限成功");
                CLog.e("get root success !");
            } else {
                ToastUtils.showToast(getApplicationContext(), "获取root失败,加壳程序可能导致读取配失败");
                CLog.e("get root fail !");
            }
        });
    }

    public  Context getMyApplicationContext(){
        return getApplicationContext();
    }
    @Override
    public void onCreate() {
        super.onCreate();

        getRoot();

        AppSecure(getApplicationContext());
    }
}
