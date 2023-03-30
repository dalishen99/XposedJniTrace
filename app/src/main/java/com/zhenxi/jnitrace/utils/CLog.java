package com.zhenxi.jnitrace.utils;

import android.util.Log;

import com.zhenxi.jnitrace.BuildConfig;


public class CLog {

    public static final String TAG = "Zhenxi";

    private static final String LOG_PATTERN = "[%s] %s";

    public static void d(String subTag, String msg) {
        if (BuildConfig.is_printf_log) {
            InfiniteLog(Log.DEBUG, TAG, String.format(LOG_PATTERN, subTag, msg));
        }
    }
    public static void d( String msg) {
        if (BuildConfig.is_printf_log) {
            InfiniteLog(Log.DEBUG, TAG, String.format(LOG_PATTERN, TAG, msg));
        }
    }

//    public static void v(String subTag, String msg) {
//        if (BuildConfig.is_printf_log) {
//            InfiniteLog(RunTimeConstants.TAG, String.format(LOG_PATTERN, subTag, msg));
//        }
//    }

    public static void w(String subTag, String msg) {
        if (BuildConfig.is_printf_log) {
            InfiniteLog(Log.WARN, TAG, String.format(LOG_PATTERN, subTag, msg));
        }
    }

    public static void i(String subTag, String msg) {
        if (BuildConfig.is_printf_log) {
            InfiniteLog(Log.INFO, TAG, String.format(LOG_PATTERN, subTag, msg));
        }
    }

    public static void i(String msg) {
        if (BuildConfig.is_printf_log) {
            InfiniteLog(Log.INFO, TAG, String.format(LOG_PATTERN, TAG, msg));
        }
    }

    public static void e(String subTag, String msg) {
        if (BuildConfig.is_printf_log) {
            InfiniteLog(Log.ERROR, TAG, String.format(LOG_PATTERN, subTag, msg));
        }
    }

    public static void e(String msg) {
        if (msg == null) {
            return;
        }
        if (BuildConfig.is_printf_log) {
            InfiniteLog(Log.ERROR, TAG, msg);
        }
    }

    public static void e(String subTag, String msg, Throwable tr) {
        if (BuildConfig.is_printf_log) {
            InfiniteLog(Log.ERROR,TAG, String.format(LOG_PATTERN, subTag, msg));
            InfiniteLog(Log.ERROR,TAG, Log.getStackTraceString(tr));
        }
    }

    public static void e(String msg, Throwable tr) {
        if (BuildConfig.is_printf_log) {
            InfiniteLog(Log.ERROR, TAG, Log.getStackTraceString(tr));
        }
    }

    //规定每段显示的长度
    private static final int LOG_MAXLENGTH = 2000;

    /**
     * log最多 4*1024 长度 这个 方法 可以解决 这个问题
     */
    private static void InfiniteLog(int logLeave, String TAG, String msg) {
        if (msg == null) {
            return;
        }
        int strLength = msg.length();
        int start = 0;
        int end = LOG_MAXLENGTH;
        for (int i = 0; i < 100; i++) {
            //剩下的文本还是大于规定长度则继续重复截取并输出
            if (strLength > end) {
                if (logLeave == Log.ERROR) {
                    Log.e(TAG, msg.substring(start, end));
                } else if (logLeave == Log.WARN) {
                    Log.w(TAG, msg.substring(start, end));
                } else if (logLeave == Log.INFO) {
                    Log.i(TAG, msg.substring(start, end));
                } else if (logLeave == Log.DEBUG) {
                    Log.d(TAG, msg.substring(start, end));
                }
                start = end;
                end = end + LOG_MAXLENGTH;
            } else {
                if (logLeave == Log.ERROR) {
                    Log.e(TAG, msg.substring(start, strLength));
                } else if (logLeave == Log.WARN) {
                    Log.w(TAG, msg.substring(start, strLength));
                } else if (logLeave == Log.INFO) {
                    Log.i(TAG, msg.substring(start, strLength));
                } else if (logLeave == Log.DEBUG) {
                    Log.d(TAG, msg.substring(start, strLength));
                }
                break;
            }
        }
    }
}
