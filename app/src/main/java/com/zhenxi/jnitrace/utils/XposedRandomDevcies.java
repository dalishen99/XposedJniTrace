package com.zhenxi.jnitrace.utils;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.lang.reflect.Method;
import java.util.Random;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;

/**
 * Created by Zhenxi on
 */
public class XposedRandomDevcies {

    private static String TAG = "lyh222222222";


    private static Class<?> BuildClass = null;

    private static Class<?> TelephonyManagerClass = null;

    private static Class<?> SettingsClass = null;

    private static Class<?> InetAddressClass = null;


    private static String[] devives = new String[]{
            "MI 9", "MI 8", "MI 6", "MI 5", "MI 4", "MI 3",
            "OPPO A5", "OPPO A37", "OPPO A59", "OPPO A59s", "OPPO A57", "OPPO A77", "OPPO R15",
            "OPPO A79", "OPPO A73", "OPPO R9", "OPPO R9plus",
            "OPPO R11", "OPPO R11plus", "OPPO R11s", "OPPO R11splus", "OPPO R11", "OPPO R15"
    };

    private static String[] changshang = new String[]{
            "Xiaomi", "OPPO", "VIVO", "HUAWEI",
    };

    private static String[] banben = new String[]{
            "4.4", "5.0", "5.1", "6.0", "7.0", "7.1", "8.0", "9.0",
    };


    private static int getRandom(int maxValue) {
        return new Random().nextInt(maxValue);
    }

    public static void RandomPhoneInfo(ClassLoader classLoader) {
        String imei = getIMEI();
        String devive = devives[getRandom(devives.length - 1)];
        String changshang = XposedRandomDevcies.changshang[getRandom(XposedRandomDevcies.changshang.length - 1)];

        try {


            BuildClass = Class.forName("android.os.Build", true, classLoader);
            if (BuildClass != null) {
                Log.e(TAG, "拿到了 手机 设备信息的 bin对象 ");
                try {
                    XposedHelpers.findAndHookMethod(BuildClass, "getString",
                            String.class,
                            new XC_MethodHook() {
                                @Override
                                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                    super.afterHookedMethod(param);
                                    String arg = (String) param.args[0];
                                    switch (arg) {
                                        case "ro.product.model":
                                            //MI 9
                                            Log.e(TAG, "获取 手机 设备信息 返回的 结果是  " + devive);
                                            param.setResult(devive);

                                            break;
                                        case "ro.product.brand": {
                                            //Xiaomi
                                            param.setResult(changshang);
                                            Log.e(TAG, "获取 手机 品牌信息  返回的结果 是    " + changshang);

                                            break;
                                        }
                                        case "ro.product.name":
                                            //cepheus
                                            Log.e(TAG, "获取 手机 产品 名字  返回的结果是   " + param.getResult().toString());
                                            break;
                                        case "ro.build.id": {
                                            String string = banben[getRandom(banben.length - 1)];
                                            param.setResult(string);
                                            Log.e(TAG, "获取 手机 版本   返回的结果是   " + string);
                                            break;
                                        }
                                        case "no.such.thing":
                                            param.setResult(System.currentTimeMillis() + "");
                                            Log.e(TAG, "获取 手机 序列号   ");
                                            break;
                                        case "ro.build.host":
                                            Log.e(TAG, "获取 手机 Host   " + param.getResult().toString());
                                            break;
                                        case "ro.build.version.release": {
                                            String string = banben[getRandom(banben.length - 1)];
                                            param.setResult(string);
                                            Log.e(TAG, "获取 安卓版本号  返回结果是  " + string);
                                            break;
                                        }
                                    }

                                }
                            }
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            TelephonyManagerClass = getClass(classLoader, TelephonyManager.class.getName());

            if (TelephonyManagerClass != null) {

                try {
                    XposedHelpers.findAndHookMethod(TelephonyManagerClass, "getDeviceId", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            param.setResult(imei);
                            Log.e(TAG, "获取getDeviceId被调用了  返回的 结果 是 " + imei);
                        }
                    });

                    XposedHelpers.findAndHookMethod(TelephonyManagerClass, "getImei", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            param.setResult(imei);
                            Log.e(TAG, "获取getImei被调用了  返回的 结果 是 " + imei);
                        }
                    });

                    XposedHelpers.findAndHookMethod(TelephonyManagerClass, "getLine1Number", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            //param.setResult(imei);
                            Log.e(TAG, "获取手机号被调用  getLine1Number");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            SettingsClass = Class.forName("android.provider.Settings", true, classLoader);

            if (SettingsClass == null) {
                SettingsClass = Class.forName("android.provider.Settings");
            }
            if (SettingsClass != null) {
                //10.0版本存在问题
                Method getStringMethod = null;
                try {
                    getStringMethod = SettingsClass.getDeclaredMethod("getString", ContentResolver.class, String.class);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                try {
                    if (getStringMethod != null) {
                        XposedHelpers.findAndHookMethod(SettingsClass,
                                "getString",
                                ContentResolver.class,
                                String.class,
                                new XC_MethodHook() {
                                    @Override
                                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                        super.afterHookedMethod(param);
                                        String string = System.currentTimeMillis() + "832";
                                        param.setResult(string);
                                        Log.e(TAG, "获取AndroidID 被调用了 返回结果是 " + string);
                                    }
                                });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            InetAddressClass = Class.forName("java.net.InetAddress", true, classLoader);

            if (InetAddressClass == null) {
                InetAddressClass = Class.forName("java.net.InetAddress");
            }
            if (InetAddressClass != null) {
                try {
                    XposedHelpers.findAndHookMethod(InetAddressClass,
                            "getHostAddress",
                            new XC_MethodHook() {
                                @Override
                                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                    super.afterHookedMethod(param);
                                    //192.168.123.71
                                    param.setResult(getRandomIp());
                                    Log.e(TAG, "获取IP地址  被调用了 返回结果是 " + param.getResult().toString());
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                //Hook 获取mac地址
                XposedHelpers.findAndHookMethod(WifiInfo.class, "getMacAddress", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        String mac = getMac();
                        param.setResult(mac);
                        Log.e(TAG, "获取MAC  被调用了 返回结果是 " + mac);

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                //Hook wifi mac 地址
                XposedHelpers.findAndHookMethod(WifiInfo.class, "getMacAddress", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        String mac = getMac();
                        param.setResult(mac);
                        Log.e(TAG, "获取MAC  被调用了 返回结果是 " + mac);

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Hook 获取IMSI地址
            try {
                XposedHelpers.findAndHookMethod(TelephonyManager.class, "getSubscriberId", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        String mac = getImsi();
                        param.setResult(mac);
                        Log.e(TAG, "获取getImsi  被调用了 返回结果是 " + mac);

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Hook 获取IMEI 26+ 以上版本的方法
            try {
                XposedHelpers.findAndHookMethod(TelephonyManager.class, "getImei", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        String mac = getIMEI();
                        param.setResult(mac);
                        Log.e(TAG, "获取getIMEI  26+  被调用了 返回结果是 " + mac);

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Hook 获取 ICCID
            try {
                XposedHelpers.findAndHookMethod(TelephonyManager.class, "getSimSerialNumber", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        String mac = getIMEI();
                        param.setResult(mac);
                        Log.e(TAG, "获取 getSimSerialNumber（ICCID）  被调用了 返回结果是 " + mac);

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Hook MSISDN
            try {
                XposedHelpers.findAndHookMethod(TelephonyManager.class, "getLine1Number", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        param.setResult("");
                        Log.e(TAG, "获取 getLine1Number（MSISDN）  ");

                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
            //Hook 运营商名字
            XposedHelpers.findAndHookMethod(TelephonyManager.class, "getSimOperatorName", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    String string = XposedRandomDevcies.changshang[getRandom(XposedRandomDevcies.changshang.length - 1)];
                    param.setResult(string);
                    Log.e(TAG, "获取 getSimOperatorName（运营商名字）");
                }
            });


            Class GSMPhoneClass = getClass(classLoader, "com.android.internal.telephony.gsm.GSMPhone");

            if (GSMPhoneClass != null) {
                //根据设备修改补充
                try {
                    XposedHelpers.findAndHookMethod(GSMPhoneClass, "getDeviceId", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            param.setResult(imei);
                            Log.e(TAG, "小于22版本号的 GSMPhone 设备ID 获取" + imei);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Class PhoneSubInfoClass = getClass(classLoader, "com.android.internal.telephony.PhoneSubInfo");
            if (PhoneSubInfoClass != null) {
                try {
                    XposedHelpers.findAndHookMethod(PhoneSubInfoClass, "getDeviceId", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            param.setResult(imei);
                            Log.e(TAG, "PhoneSubInfo 设备ID 获取" + imei);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Class PhoneProxyClass = getClass(classLoader, "com.android.internal.telephony.PhoneProxy");
            if (PhoneProxyClass != null) {
                try {
                    XposedHelpers.findAndHookMethod(PhoneProxyClass, "getDeviceId", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            param.setResult(imei);
                            Log.e(TAG, "PhoneProxy 设备ID 获取" + imei);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Class WifiInfoClass = getClass(classLoader, WifiInfo.class.getName());
            if (WifiInfoClass != null) {
                try {
                    XposedHelpers.findAndHookMethod(WifiInfoClass, "getMacAddress", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            //param.setResult(imei);
                            Log.e(TAG, "getMacAddress Mac 地址 被调用");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Class DevicePolicyManagerClass = getClass(classLoader, "android.app.admin.DevicePolicyManager");
            if (DevicePolicyManagerClass != null) {
                try {
                    Method getWifiMacAddress = DevicePolicyManagerClass.getDeclaredMethod("getWifiMacAddress",
                            ComponentName.class);
                    if(getWifiMacAddress!=null) {
                        XposedHelpers.findAndHookMethod(DevicePolicyManagerClass,
                                "getWifiMacAddress",
                                ComponentName.class,
                                new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                super.afterHookedMethod(param);
                                //param.setResult(imei);
                                Log.e(TAG, "getWifiMacAddress 地址 被调用");
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Class BluetoothAdapterClass = getClass(classLoader, "android.bluetooth.BluetoothAdapter");
            if (BluetoothAdapterClass != null) {
                try {
                    XposedHelpers.findAndHookMethod(BluetoothAdapterClass, "getAddress", new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            super.afterHookedMethod(param);
                            //param.setResult(imei);
                            Log.e(TAG, "蓝牙地址 被调用  ");
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        } catch (Throwable e) {
            Log.e(TAG, "初始化 设备信息  异常  " + e.getMessage());
            e.printStackTrace();
        }

    }

    private static Class getClass(ClassLoader classLoader, String classPath)  {
        try {
            Class aClass = Class.forName(classPath, false, classLoader);
            if (aClass == null) {
                aClass = Class.forName(classPath);
            }
            if(aClass!=null){
                return aClass;
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 随机Imsi
     */
    private static String getImsi() {
        // 460022535025034
        String title = "4600";
        int second = 0;
        do {
            second = new Random().nextInt(8);
        } while (second == 4);
        int r1 = 10000 + new Random().nextInt(90000);
        int r2 = 10000 + new Random().nextInt(90000);
        return title + "" + second + "" + r1 + "" + r2;
    }

    /**
     * 随机MAC
     */
    private static String getMac() {
        char[] char1 = "abcdef".toCharArray();
        char[] char2 = "0123456789".toCharArray();
        StringBuffer mBuffer = new StringBuffer();
        for (int i = 0; i < 6; i++) {
            int t = new Random().nextInt(char1.length);
            int y = new Random().nextInt(char2.length);
            int key = new Random().nextInt(2);
            if (key == 0) {
                mBuffer.append(char2[y]).append(char1[t]);
            } else {
                mBuffer.append(char1[t]).append(char2[y]);
            }

            if (i != 5) {
                mBuffer.append(":");
            }
        }
        return mBuffer.toString();
    }

    /**
     * 随机IMEI
     *
     * @return
     */
    private static String getIMEI() {// calculator IMEI
        int r1 = 1000000 + new Random().nextInt(9000000);
        int r2 = 1000000 + new Random().nextInt(9000000);
        String input = r1 + "" + r2;
        char[] ch = input.toCharArray();
        int a = 0, b = 0;
        for (int i = 0; i < ch.length; i++) {
            int tt = Integer.parseInt(ch[i] + "");
            if (i % 2 == 0) {
                a = a + tt;
            } else {
                int temp = tt * 2;
                b = b + temp / 10 + temp % 10;
            }
        }
        int last = (a + b) % 10;
        if (last == 0) {
            last = 0;
        } else {
            last = 10 - last;
        }
        return input + last;
    }

    /**
     * 应用程序运行命令获取 Root权限，设备必须已破解(获得ROOT权限)
     *
     * @return 应用程序是/否获取Root权限
     */
    public static boolean upgradeRootPermission(String pkgCodePath) {
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd = "chmod 777 " + pkgCodePath;
            process = Runtime.getRuntime().exec("su"); //切换到root帐号
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
            }
        }
        return true;
    }

    public void getRoot(Context context) {
        String apkRoot = "chmod 777 " + context.getPackageCodePath();
        boolean b = upgradeRootPermission(apkRoot);
        if (b) {
            Toast.makeText(context, "以获取root权限", Toast.LENGTH_LONG).show();
        }
        return;
    }

    public static String getAndroidId(Context context) {
        String ANDROID_ID = Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID);
        return ANDROID_ID;
    }

    public static String getRandomIp() {

        // 需要排除监控的ip范围
        int[][] range = {{607649792, 608174079}, // 36.56.0.0-36.63.255.255
                {1038614528, 1039007743}, // 61.232.0.0-61.237.255.255
                {1783627776, 1784676351}, // 106.80.0.0-106.95.255.255
                {2035023872, 2035154943}, // 121.76.0.0-121.77.255.255
                {2078801920, 2079064063}, // 123.232.0.0-123.235.255.255
                {-1950089216, -1948778497}, // 139.196.0.0-139.215.255.255
                {-1425539072, -1425014785}, // 171.8.0.0-171.15.255.255
                {-1236271104, -1235419137}, // 182.80.0.0-182.92.255.255
                {-770113536, -768606209}, // 210.25.0.0-210.47.255.255
                {-569376768, -564133889}, // 222.16.0.0-222.95.255.255
        };

        Random rdint = new Random();
        int index = rdint.nextInt(10);
        String ip = num2ip(range[index][0] + new Random().nextInt(range[index][1] - range[index][0]));
        return ip;
    }

    /*
     * 将十进制转换成IP地址
     */
    public static String num2ip(int ip) {
        int[] b = new int[4];
        String x = "";
        b[0] = (int) ((ip >> 24) & 0xff);
        b[1] = (int) ((ip >> 16) & 0xff);
        b[2] = (int) ((ip >> 8) & 0xff);
        b[3] = (int) (ip & 0xff);
        x = Integer.toString(b[0]) + "." + Integer.toString(b[1]) + "." + Integer.toString(b[2]) + "." + Integer.toString(b[3]);

        return x;
    }

    /**
     * 打印 全部的 方法信息
     * 主要用于打印 class里面的方法信息
     */
    public static void getClassMethodInfo(Class mClass) {
        if (mClass == null) {
            return;
        }
        Method[] declaredMethods = mClass.getDeclaredMethods();

        for (Method method : declaredMethods) {
            String Parameter = "";
            //参数类型
            for (Class m : method.getParameterTypes()) {
                Parameter = Parameter + m.getName() + ",";
            }
            CLog.e("该方法名字 " + method.getName() + " 参数类型 " + Parameter);
        }
    }
}
