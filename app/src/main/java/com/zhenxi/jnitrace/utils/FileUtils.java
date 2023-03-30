package com.zhenxi.jnitrace.utils;



import android.content.Context;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * QC api封装专属 FileUtils
 */
public class FileUtils {

    private static FileOutputStream outStream = null;
    private static OutputStreamWriter writer = null;
    /**
     * 递归删除
     * 删除某个目录及目录下的所有子目录和文件
     * @param file 文件或目录
     * @return 删除结果
     */
    public static boolean delFiles(File file,boolean isDelSelf){
        boolean result = false;
        //目录
        if(file.isDirectory()){
            File[] childrenFiles = file.listFiles();
            for (File childFile:childrenFiles){
                result = delFiles(childFile,true);
                if(!result){
                    return result;
                }
            }
        }
        if(isDelSelf) {
            //删除 文件、空目录
            result = file.delete();
        }
        return result;
    }
    public static File makeSureDirExist(File file) {

        if(file.getPath().equals("")){
            return file;
        }
        if (file.exists() && file.isFile()) {
            return file;
        }
        file.mkdirs();
        try {
            file.setExecutable(true,false);
            file.setWritable(true,false);
            file.setReadable(true,false);
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        return file;
    }
    public static void saveString(File saveFile, String str) {
        FileWriter fw = null;
        try {
            fw = new FileWriter(saveFile, false);
            PrintWriter pw = new PrintWriter(fw);
            pw.println(str);
            pw.flush();
            try {
                fw.flush();
                pw.close();
                fw.close();
            } catch (IOException e) {

            }
        } catch (IOException e) {

        }
    }
    public static String readToString(File file) throws IOException {
        try (InputStream is = new FileInputStream(file.getPath());
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            int i;
            while ((i = is.read()) != -1) {
                baos.write(i);
            }
            return baos.toString();
        }
    }
    /*
     * 用于一个文件保存大量字符串
     * 不进行关闭,防止反复创建影响效率
     */
    public static void saveStringNoClose(String str, File file) {
        try {
            if(str==null||str.length()==0){
                return;
            }
            if(!file.exists()){
                boolean mkdirs = Objects.requireNonNull(file.getParentFile()).mkdirs();
                boolean newFile = file.createNewFile();
            }
            if (outStream == null) {
                outStream = new FileOutputStream(file, true);
                writer = new OutputStreamWriter(outStream, StandardCharsets.UTF_8);
            }
            writer.write(str);
            writer.flush();
        } catch (Throwable ignored) {

        }
    }
    public static void saveStringClose(){
        try {
            if(outStream!=null){
                outStream.close();
            }
            if(writer!=null){
                writer.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
