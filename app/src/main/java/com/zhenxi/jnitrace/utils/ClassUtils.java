package com.zhenxi.jnitrace.utils;

import android.os.Build;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import de.robv.android.xposed.XposedHelpers;

/**
 * @author Zhenxi on 2020-07-18
 */
public class ClassUtils {

    private static Method forNameMethod = null;
    private static Method forNameMethodThree = null;

    private static Method invokeMethod = null;

    static {
        try {
            forNameMethodThree = Class.class.getDeclaredMethod("forName", String.class, boolean.class, ClassLoader.class);
            forNameMethodThree.setAccessible(true);
            forNameMethod = Class.class.getDeclaredMethod("forName", String.class);
            forNameMethod.setAccessible(true);
            invokeMethod = Method.class.getDeclaredMethod("invoke", Object.class, Object[].class);
            invokeMethod.setAccessible(true);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }


    public static void getClassMethodInfo(Class c) {
        if (c == null) {
            return;
        }
        for (Method method : c.getDeclaredMethods()) {
            CLog.e("方法名字 " + method.getName() + " 方法参数 " + Arrays.toString(method.getParameterTypes()));
        }
        CLog.e("----------------------------------------------------------------------------------------------");

    }

    public static void getClassFieldInfo(Class c) {
        if (c == null) {
            return;
        }
        for (Field method : c.getDeclaredFields()) {
            CLog.e("字段类型 " + method.getName() + " 方法参数 " + method.getType().getName());
        }
        CLog.e("----------------------------------------------------------------------------------------------");

    }

    private static Class<?> findInChoose(String classname, boolean isloader) {
        Class<?> invokeClass = null;
        if (Build.VERSION.SDK_INT >= 28) {
            if (classloaderList == null) {
                //如果找不到则直接去内存里面查找
                classloaderList = ChooseUtils.choose(ClassLoader.class, true);
            }
            //直接遍历
            for (Object classloader : classloaderList) {
                ClassLoader myCLassloader = (ClassLoader) classloader;
                try {
                    invokeClass = Class.forName(classname, isloader, myCLassloader);
                    //查找到直接返回
                    if (invokeClass != null) {
                        return invokeClass;
                    }
                } catch (ClassNotFoundException classNotFoundException) {

                }
            }
        } else {
            try {
                invokeClass = Class.forName(classname, isloader, ClassLoader.getSystemClassLoader());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return invokeClass;
    }

    /**
     * 存放当前进程全部的classloader
     */
    public static ArrayList<Object> classloaderList = null;

    public static Class<?> forName(String classname, boolean isloader, ClassLoader loader) {
        if(classname==null){
            return null;
        }
        //pass bangbang
        if(classname.contains("secneo")||classname.contains("apkwrapper")){
            return null;
        }
        Class<?> invokeClass;
        if (loader == null) {
            invokeClass = findInChoose(classname, isloader);
            return invokeClass;
        }
        try {
            invokeClass = Class.forName(classname, isloader, loader);
        } catch (Throwable e) {
            invokeClass = findInChoose(classname, isloader);
        }
        try {
            if (invokeClass == null) {
                invokeClass = findInChoose(classname, isloader);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        if (invokeClass == null) {
            CLog.e("class utils not found class ->  " + classname);
        }

        return invokeClass;
    }

    public static Class forName(String classname) {
        if (classname == null || classname.equals("")) {
            return null;
        }
        Class invokeClass = null;
        try {
            invokeClass = (Class) forNameMethod.invoke(null, classname);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return invokeClass;
    }


    public static Class forNameAndInit(String classname, ArrayList<ClassLoader> classloaderList) {
        if (classname == null || classname.equals("") || classloaderList == null || classloaderList.size() == 0) {
            return null;
        }
        //CLogUtils.e("当前ClassloaderList 的个数  "+classloaderList.size());
        try {
            for (ClassLoader classLoader : classloaderList) {
                try {
                    Class invokeClass = XposedHelpers.findClass(classname, classLoader);
                    if (invokeClass != null) {
                        return invokeClass;
                    }
                } catch (Throwable e) {

                }
            }

        } catch (Throwable e) {
            CLog.e("forNameAndInit error   " + e.getMessage());
        }
        return null;
    }

    public static Class forNameAndInit(String classname, ClassLoader classloader) {
        if (classname == null || classname.equals("") || classloader == null) {
            return null;
        }
        Class invokeClass = null;
        try {
            invokeClass = XposedHelpers.findClass(classname, classloader);
            if (invokeClass != null) {
                return invokeClass;
            }
        } catch (Throwable ignored) {

        }
        return null;
    }


    public static Object invoke(Method method, Object object, Object... agrs) {
        Object resut = null;
        try {
            resut = invokeMethod.invoke(method, object, agrs);
        } catch (Throwable e) {
            //CLogUtils.e("反射失败 " + e.getMessage());
            e.printStackTrace();
        }
        return resut;
    }
}
