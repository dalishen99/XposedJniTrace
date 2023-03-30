package com.zhenxi.jnitrace.utils;


import com.zhenxi.external.gson.Gson;
import com.zhenxi.external.gson.GsonBuilder;

/**
 * Created by Zhenxi on
 * 2019/11/12
 */
public class GsonUtils {


    public static final Gson gson =  new GsonBuilder()
                                    .setLenient()
                                    .create();


    public static <T>T str2obj(String jsonString, Class<T> c) {
        try {
            return gson.fromJson(jsonString, c);
        } catch (Throwable e) {
            return null;
        }
    }

    public static String obj2str(Object object) {
        try {
            if (object == null) {
                return null;
            }
            return gson.toJson(object);
        } catch (Throwable e) {
            return null;
        }
    }
}
