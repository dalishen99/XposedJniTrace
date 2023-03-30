package com.zhenxi.jnitrace.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Zhenxi on 2021/7/4
 */
public class UnZipUtils {



    /**
     * 解压zip到指定的路径
     */
    public  static  void UnZipFolder(String zipFileString, String outPathString)  {
        CLog.i("UnZipFolder  zipFileString -> "+zipFileString+"  outPathString -> "+outPathString);
        File file1 = new File(outPathString);
        if(file1.exists()){
            CLog.i("UnZipFolder exists ,start FileUtils -> delete  "+outPathString+"  "+
                    FileUtils.delFiles(file1,false)
            );
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(zipFileString);
            ZipInputStream inZip = new ZipInputStream(fileInputStream);
            ZipEntry zipEntry;
            String szName;

            while ((zipEntry = inZip.getNextEntry()) != null) {
                szName = zipEntry.getName();
                if (zipEntry.isDirectory()) {
                    //获取部件的文件夹名
                    szName = szName.substring(0, szName.length() - 1);
                    File folder = new File(outPathString + File.separator + szName);
                    FileUtils.makeSureDirExist(folder);
                } else {
                    if(!szName.contains("lib")){
                        continue;
                    }
                    File file = new File(outPathString + File.separator + szName);
                    if (!file.exists()){
                        file.getParentFile().mkdirs();
                        file.createNewFile();
                    }
                    // 获取文件的输出流
                    FileOutputStream out = new FileOutputStream(file);
                    int len;
                    byte[] buffer = new byte[1024];
                    // 读取（字节）字节到缓冲区
                    while ((len = inZip.read(buffer)) != -1) {
                        // 从缓冲区（0）位置写入（字节）字节
                        out.write(buffer, 0, len);
                        out.flush();
                    }
                    out.close();
                }
            }
            inZip.close();
            fileInputStream.close();
        } catch (Throwable e) {
            CLog.i("UnZipFolder error "+e.getMessage());
            e.printStackTrace();
        }
    }
}
