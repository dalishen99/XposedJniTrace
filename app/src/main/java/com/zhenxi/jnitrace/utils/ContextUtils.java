package com.zhenxi.jnitrace.utils;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Build;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

/**
 * @author Zhenxi on 2022/4/5
 */
public class ContextUtils {
    @SuppressLint("StaticFieldLeak")
    private static Context sContext = null;

    static {
        try {
            if (Build.VERSION.SDK_INT >= 28) {
                //pass hide api check
                HiddenApiBypass.addHiddenApiExemptions("");
            }
        } catch (Exception exception) {
            CLog.e("ContextUtils static addHiddenApiExemptions error "+exception.getMessage());
        }
    }

    public static Context getContext(){
        if(sContext!=null){
            return sContext;
        }
        //Android 9.0以上可以动态内存去查找
        if (getSDKVersion() >= 28) {
            //先尝试查找Application
            ArrayList<Object> ApplicationChoose = ChooseUtils.choose(Application.class);
            if (ApplicationChoose != null && ApplicationChoose.size() >= 1) {
                sContext = ((Application) ApplicationChoose.get(0)).getApplicationContext();
                return sContext;
            }
            //查找Context
            if (sContext == null) {
                ArrayList<Object> ContextChoose = ChooseUtils.choose(Context.class, true);
                if (ContextChoose != null && ContextChoose.size() >= 1) {
                    sContext = (Context) ContextChoose.get(0);
                    return sContext;
                }
            }
        }
        //上述都不行在尝试创建
        sContext = getContextInThread();

        return sContext;
    }
    private static Context getContextInThread()  {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread", new Class[0]);
            currentActivityThreadMethod.setAccessible(true);
            Object currentActivityThread = currentActivityThreadMethod.invoke(null, new Object[0]);
            Field declaredField = activityThreadClass.getDeclaredField("mBoundApplication");
            declaredField.setAccessible(true);
            Object mBoundApplication = declaredField.get(currentActivityThread);
            Field applicationInfoField = mBoundApplication.getClass().getDeclaredField("info");
            applicationInfoField.setAccessible(true);
            Object applicationInfo = applicationInfoField.get(mBoundApplication);
            Method createAppContext = Class.forName("android.app.ContextImpl").getDeclaredMethod("createAppContext",new Class[]{activityThreadClass, applicationInfo.getClass()});
            createAppContext.setAccessible(true);
            return (Context) createAppContext.invoke(null, new Object[]{currentActivityThread, applicationInfo});
        } catch (Throwable e) {
            CLog.e(" getContextInThread error "+e);
        }
        return null;
    }
    public static int getSDKVersion() {
        return Build.VERSION.SDK_INT;
    }
}
