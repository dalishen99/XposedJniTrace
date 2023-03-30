package com.zhenxi.jnitrace.bean;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

/**
 * Created by lyh on 2019/2/14.
 */

public class AppBean {


    public String appName;

    public String packageName;

    public Drawable appIcon;


    public boolean isSystemApp=false;


    @NonNull
    @Override
    public String toString() {
        return "AppBean{" +
                "appName='" + appName + '\'' +
                ", packageName='" + packageName + '\'' +
                ", appIcon=" + appIcon +
                ", isSystemApp=" + isSystemApp +
                '}';
    }
}
