package com.zhenxi.jnitrace.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by fullcircle on 2016/12/31.
 */

public class ToastUtils {

    private static Toast toast;

    public static void showToast(Context context, String msg) {
        try {
            if (context == null) {
                return;
            }
            ThreadUtils.runOnMainThread(new Runnable() {
                @Override
                public void run() {
                    if (toast == null) {
                        toast = Toast.makeText(context, msg, Toast.LENGTH_LONG);
                    }else {
                        toast.setText(msg);
                    }
                    toast.show();
                }
            });

        } catch (Throwable e) {
            CLog.e("showToast error " + e,e);
        }
    }
}
