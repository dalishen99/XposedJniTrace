package com.zhenxi.jnitrace.utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

/**
 * Created by Lyh on
 * 2019/10/28
 */
public class RootUtils {
    public static boolean checkGetRootAuth() {
        Process process = null;
        DataOutputStream os = null;
        try {
            try {
                process = Runtime.getRuntime().exec("su");
            } catch (Throwable e) {
                CLog.e("exec error  " + e);
                e.printStackTrace();
            }

            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("exit\n");
            os.flush();
            int exitValue = process.waitFor();
            if (exitValue == 0) {
                CLog.e("checkGetRootAuth  ture");

                return true;
            } else {
                return false;
            }
        } catch (Throwable e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

        } catch (Exception e) {
            CLog.e("get root error 111111 " + e.getMessage());
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (process != null) {
                    process.destroy();
                }
            } catch (Exception e) {
                CLog.e("get root error 222222" + e.getMessage());
            }
        }
        return true;
    }

    public static void execShell(String cmd){
        OutputStream outputStream=null;
        DataOutputStream dataOutputStream=null;
        try{

            Runtime runtime = Runtime.getRuntime();
            runtime.exec(cmd+"\n");
            //权限设置
            Process p = Runtime.getRuntime().exec("su");
            //获取输出流
            outputStream =  p.getOutputStream();
            dataOutputStream =new DataOutputStream(outputStream);
            //将命令写入
            dataOutputStream.writeBytes(cmd+ "\n");
            //提交命令
            dataOutputStream.flush();

        }
        catch(Throwable t){
            CLog.e("execShell error "+t.getMessage());
            t.printStackTrace();
        }finally {
           if(outputStream!=null){
               try {
                   outputStream.close();
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
           if(dataOutputStream!=null){
               try {
                   dataOutputStream.close();
               } catch (IOException e) {
                   e.printStackTrace();
               }
           }
        }
    }
}
