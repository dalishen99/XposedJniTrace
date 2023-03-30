//
// Created by zhenxi on 2021/11/7.
//

#include "includes/fileUtils.h"
#include "includes/appUtils.h"

#include <libgen.h>

using namespace std;

/**
 * 根据fd获取path路径
 */
string fileUtils::get_file_name(int fd, pid_t pid) {
    if (fd <= 0) {
        LOGE("fileUtils get_file_name error fd == 0  -> %d ", fd);
        return {};
    }
    const char *path = string("/proc/").append(to_string(pid))
            .append("/fd/").append(to_string(fd)).c_str();
    char file_path[PATH_MAX] = {'0'};
    if (readlink(path, file_path, sizeof(file_path) - 1) != -1) {
        return {file_path};
    }
    LOGE("fileUtils get_file_name not found fd %d path  %s ", fd, strerror(errno));
    return {};
}

/*
 * 创建多级dir目录,防止创建文件之前找不到目录
 */
int fileUtils::makeDir(const char *path) {

    size_t beginCmpPath;
    size_t endCmpPath;
    size_t fullPathLen;
    size_t pathLen = strlen(path);
    char currentPath[128] = {0};
    char fullPath[128] = {0};
    //相对路径
    if ('/' != path[0]) {
        //获取当前路径
        getcwd(currentPath, sizeof(currentPath));
        strcat(currentPath, "/");
        beginCmpPath = strlen(currentPath);
        strcat(currentPath, path);
        if (path[pathLen] != '/') {
            strcat(currentPath, "/");
        }
        endCmpPath = strlen(currentPath);

    } else {
        //绝对路径
        strcpy(currentPath, path);
        if (path[strlen(path)] != '/') {
            strcat(currentPath, "/");
        }
        beginCmpPath = 1;
        endCmpPath = strlen(currentPath);
    }
    //创建各级目录
    for (int i = beginCmpPath; i < endCmpPath; i++) {
        if ('/' == currentPath[i]) {
            currentPath[i] = '\0';
            if (access(currentPath, NULL) != 0) {
                if (mkdir(currentPath, 0755) == -1) {
                    LOGE("fileUtils mkdir error ,currentPath -> %s  %s ", currentPath,
                         strerror(errno));
                    return -1;
                }
            }
            currentPath[i] = '/';
        }
    }
    return 0;
}


string fileUtils::readText(string file) {
    std::ifstream infile;
    infile.open(file.data());   //将文件流对象与文件连接起来
    if (!infile.is_open()) {
        //若失败,则输出错误消息,并终止程序运行
        //LOGE("fileUtils read text open file error %s ", file.c_str());
        return {};
    }

    string result;
    string lineStr;
    while (getline(infile, lineStr)) {
        result.append(lineStr);
    }
    infile.close();             //关闭文件输入流
    return result;
}

string fileUtils::readText(FILE *file) {
    string result;
    char buf[PATH_MAX] = {0};
    while (fgets(buf, PATH_MAX, file)) {
        result.append(buf);
    }
    return result;
}

void fileUtils::writeText(string file, const string& str, bool isAppend) {
    std::ofstream os;
    //保证文件存在
    makeDir(dirname(file.c_str()));
    if (isAppend) {
        //正常打开,末尾追加
        os.open(file.data(), ios::app);
    } else {
        //覆盖
        os.open(file.data(), ios::trunc);
    }
    os << str;
    os.close();             //关闭文件输入流
}

bool fileUtils::savefile(const char *savePath, size_t size, size_t start, bool isDele) {
    if (size == 0) {
        LOGE("savefile size == 0  ")
        return false;
    }
    if (savePath == nullptr) {
        LOGE("filePath == nullptr ")
        return false;
    }
    char *path = strdup(savePath);
    char *filepath = dirname(path);
    LOGI("fileUtils::savefile upper path -> %s ", filepath)
    if (access(filepath, 0) == -1) {
        LOGI("fileUtils::savefile not found upper path ,start create  %s", filepath)
        //如果父文件夹不存在递归创建多级
        int createret = fileUtils::makeDir(filepath);
        if (createret == -1) {
            LOGE("fileUtils::savefile create upper path error  -> %s  %s ", filepath,
                 strerror(errno))
            return false;
        }
    }
    FILE *file;
    if (isDele) {
        //http://www.wenjuntech.com/wp-content/uploads/2018/05/QQjt20180424183202-99.jpg
        //清除原有数据,不存在则创建
        file = fopen(savePath, "w+");
    } else {
        //不清除原有数据,不存在则创建
        file = fopen(savePath, "wt+");
    }
    if (file == nullptr) {
        LOGE("savefile fopen == null %s  ", strerror(errno))
        return false;
    }
    fseek(file, 0, SEEK_SET);

    LOGI("fileUtils::savefile save path -> start address ->0x%x  file end address -> 0x%x ", start,
         (start + size))
    //有的SO的内存段是不可读不可写,必须RWX 只R不行
    MPROTECT(start, size, MEMORY_RWX);

    size_t wirtesize = fwrite(reinterpret_cast<void *>(start), size, 1, file);

    fclose(file);
    if (path != nullptr) {
        free(path);
    }
    LOGI("fileUtils::savefile sucess ! save size %s  %zu fwrite size -> %lu ", path, size,
         wirtesize)
    return true;
}

int fileUtils::copy_file(const char *SourceFile, const char *TargetFile) {
    // 创建 std::fstream 流对象
    std::ifstream in;
    std::ofstream out;

    try {
        // 打开源文件
        in.open(SourceFile, std::ios::binary);
        // 打开源文件失败
        if (in.fail()) {
            LOGE("fileUtils::Fail to open the source file %s", SourceFile)
            //std::cout << "Error 1: Fail to open the source file." << std::endl;
            // 关闭文件对象
            in.close();
            out.close();
            return 0;
        }
        out.open(TargetFile, std::ios::binary);
        if (out.fail()) {
            //std::cout << "Error 2: Fail to create the new file." << std::endl;
            LOGE("fileUtils::Fail to create the new file %s", SourceFile)
            in.close();
            out.close();
            return 0;
        } else {
            out << in.rdbuf();
            out.close();
            in.close();
            return 1;
        }
    } catch (std::exception &E) {
        LOGE("fileUtils::copy_file error %s ", E.what())
//        std::cout << E.what() << std::endl;
        return 1;
    }
}

bool fileUtils::isFileExists(string name) {
    ifstream f(name.c_str());
    return f.good();
}
