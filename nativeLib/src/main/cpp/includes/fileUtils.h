//
// Created by zhenxi on 2021/11/7.
//


#ifndef QCONTAINER_PRO_FILEUTILS_H
#define QCONTAINER_PRO_FILEUTILS_H


#include <cstdio>
#include "AllInclude.h"

class fileUtils {
public:
    static int makeDir(const char* path);
    static string readText(string file);
    static string readText(FILE *file);
    static void writeText(string file,const string& str,bool isAppend);
    static bool savefile(const char* filePath,size_t size,size_t start,bool isDele);
    static string get_file_name(int fd,pid_t pid);
    static int copy_file(const char* SourceFile, const char* TargetFile);
    static bool isFileExists(string name);
};


#endif //QCONTAINER_PRO_FILEUTILS_H
