//
// Created by zhenxi on 2022/1/21.
//
#include <iosfwd>
#include <iostream>
#include <string>
#include <map>
#include <list>
#include <jni.h>
#include <dlfcn.h>
#include <cstddef>
#include <fcntl.h>
#include <dirent.h>
#include <sys/syscall.h>
#include <unistd.h>
#include <sstream>
#include <ostream>

#include <cstdlib>
#include <sys/ptrace.h>
#include <sys/stat.h>
#include <syscall.h>
#include <climits>
#include <sys/socket.h>
#include <sys/wait.h>
#include <sys/user.h>
#include <pthread.h>
#include <vector>
#include <zlib.h>
#include <list>
#include <string>
#include <map>
#include <list>

#include <cstring>
#include <cstdio>
#include <regex>
#include <cerrno>
#include <climits>
#include <iostream>
#include <fstream>

#include "includes/stringHandler.h"
#include "HookUtils.h"
#include "mylibc.h"
#include "stringUtils.h"
#include "dlfcn_compat.h"
#include "libpath.h"


using namespace StringUtils;
#define HOOK_SYMBOL_DOBBY(handle, func)  \
  hook_libc_function(handle, #func, (void*) new_##func, (void**) &orig_##func); \





# define DL_INFO \
    Dl_info info; \
    dladdr((void *) __builtin_return_address(0), &info); \

# define IS_MATCH \
        if(isLister(&info,info.dli_fname)){ \
        { \

# define IS_NULL(value) \
    value == nullptr?"":value\

namespace ZhenxiRunTime::stringHandlerHook {

    static std::ofstream *hookStrHandlerOs;
    static std::list<string> filterSoList;
    static std::list<string> forbidSoList;
    static bool isHookAll = false;

    static bool isSave = false;
    static string match_so_name = {};
    static std::mutex supernode_ids_mux_;


    __always_inline
    static bool isAppFile(const char *path) {
        if (my_strstr(path, "/data/") != nullptr) {
            return true;
        }
        return false;
    }
    __always_inline
    static string getFileNameForPath(const char *path) {
        std::string pathStr = path;
        size_t pos = pathStr.rfind('/');
        if (pos != std::string::npos) {
            return pathStr.substr(pos + 1);
        }
        return pathStr;
    }
    __always_inline
    static inline bool isLister(Dl_info *info, const char *name) {
        if (isHookAll) {
            if (!isAppFile(name)) {
                return false;
            }
            for (const string &forbid: forbidSoList) {
                if (my_strstr(name, forbid.c_str()) != nullptr) {
                    //找到了则不进行处理
                    return false;
                }
            }
            match_so_name = getFileNameForPath(name);
            return true;
        } else {
            for (const string &filter: filterSoList) {
                if (my_strstr(name, filter.c_str()) != nullptr) {
                    match_so_name = getFileNameForPath(name);
                    return true;
                }
            }
            return false;
        }
    }



    bool getpData(char temp[],const void *p, size_t size) {
        memset(temp,0,strlen(temp));
        int i;
        int len = 0;
        for (i = 0; i < size; i++) {
            len += sprintf(temp + len, "%02X", ((char *) p)[i]);
        }
        return true;
    }

    static void write(const std::string &msg) {
        //写入方法加锁,防止多进程导致问题
        //std::unique_lock<std::mutex> mock(supernode_ids_mux_);
        if (msg.c_str() == nullptr||msg.empty()) {
            return;
        }
        auto &info = string("[").append(
                match_so_name).append("]").append(msg);
        if (isSave) {
            if (hookStrHandlerOs != nullptr) {
                (*hookStrHandlerOs) << info;
            }
        }
        LOG(INFO) << info;
    }


    //void __init(const value_type* __s, size_type __sz);
    HOOK_DEF(void*, string__init_1, void *thiz, char const *__s, size_t __sz) {
        DL_INFO
        IS_MATCH
                write(*((string *) thiz));
            }
        }
        return orig_string__init_1(thiz, __s, __sz);
    }

    //void __init(const value_type* __s, size_type __sz, size_type __reserve);
    HOOK_DEF(void*, string__init_2, void *thiz, char const *__s, size_t __sz,
             size_t __reserve) {
        DL_INFO
        IS_MATCH
                write(*((string *) thiz));
            }
        }
        return orig_string__init_2(thiz, __s, __sz, __reserve);
    }
    //void __init(size_type __n, value_type __c);
    HOOK_DEF(void*, string__init_3, void *thiz, size_t __n, char __c) {
        DL_INFO
        IS_MATCH
                write(*((string *) thiz));
            }
        }
        return orig_string__init_3(thiz, __n, __c);
    }




    //append(const value_type* __s, size_type __n)
    HOOK_DEF(void*, string__append_1, void *thiz, char const *__s, size_t __n) {
        LOGI("stringHanderUtilsCallBack append1111  ")

        DL_INFO
        IS_MATCH
                write(*((string *) thiz));
            }
        }
        return orig_string__append_1(thiz, __s, __n);
    }

    //append(const value_type* __s)
    HOOK_DEF(void*, string__append_2, void *thiz, char const *__s) {
        LOGI("stringHanderUtilsCallBack append2222  ")

        DL_INFO
        IS_MATCH
                write(*((string *) thiz));
            }
        }
        return orig_string__append_2(thiz, __s);
    }
    //basic_string<_CharT, _Traits, _Allocator>::~basic_string()
    HOOK_DEF(void*, destroy, void *thiz) {
        LOGI("stringHanderUtilsCallBack destroy  11111111111")
//        DL_INFO
//        IS_MATCH
//                write(*((string *) thiz));
//            }
//        }
        return orig_destroy(thiz);
    }





    // char* strstr(char* h, const char* n)
    HOOK_DEF(char*, strstr, char *h, const char *n) {
        DL_INFO
        IS_MATCH
                char *ret = orig_strstr(h, n);
                auto strInfo = string("strstr() arg1 -> ").append(IS_NULL(h)).append(
                        "  arg2-> ").append(IS_NULL(n));
                if (ret != nullptr) {
                    strInfo.append("result -> ").append(IS_NULL(ret));
                }
                strInfo.append("\n");
                write(strInfo);
                return ret;
            }
        }
        return orig_strstr(h, n);
    }

    //size_t strlen(const char* __s)
    HOOK_DEF(size_t, strlen, const char *__s) {
        DL_INFO
        IS_MATCH
                size_t ret = orig_strlen(__s);
                auto strInfo = string("strstr() arg1 -> ").append(IS_NULL(__s));
                strInfo.append("\n");
                write(strInfo);
                return ret;
            }
        }
        return orig_strlen(__s);
    }

    //char* strcat(char* __dst, const char* __src);
    HOOK_DEF(char *, strcat, char *__dst, const char *__src) {
        DL_INFO
        IS_MATCH
                char *ret = orig_strcat(__dst, __src);
                auto strInfo = string("strcat() arg1 -> ").append(IS_NULL(__dst)).append(
                        "  arg2-> ").append(IS_NULL(__src));
                if (ret != nullptr) {
                    strInfo.append("result -> ").append(IS_NULL(ret));
                }
                strInfo.append("\n");
                write(strInfo);
                return ret;
            }
        }
        return orig_strcat(__dst, __src);
    }
    //int strcmp(const char* __lhs, const char* __rhs)
    HOOK_DEF(int, strcmp, const char *__lhs, const char *__rhs) {

        DL_INFO
        IS_MATCH
                int ret = orig_strcmp(__lhs, __rhs);
                auto strInfo = string("strcmp() arg1 -> ").append(IS_NULL(__lhs)).append(
                        "  arg2-> ").append(IS_NULL(__rhs));
                strInfo.append("\n");
                write(strInfo);
                return ret;
            }
        }
        return orig_strcmp(__lhs, __rhs);
    }
    //char* strcpy(char* __dst, const char* __src);
    HOOK_DEF(char *, strcpy, char *__dst, const char *__src) {

        DL_INFO
        IS_MATCH
                char *ret = orig_strcpy(__dst, __src);
                auto strInfo = string("strcpy() arg1 -> ").append(IS_NULL(__dst)).append(
                        "  arg2-> ").append(IS_NULL(__src));
                if (ret != nullptr) {
                    strInfo.append("result -> ").append(IS_NULL(ret));
                }
                strInfo.append("\n");
                write(strInfo);
                return ret;
            }
        }
        return orig_strcpy(__dst, __src);
    }
    //int sprintf(char* __s, const char* __fmt, ...)
    HOOK_DEF(int, sprintf, char *__s, const char *__fmt, char *p...) {
        DL_INFO
        IS_MATCH
                int ret = orig_sprintf(__s, __fmt, p);

                auto strInfo = string("sprintf() arg1 -> ").append(IS_NULL(__fmt)).append(
                        "  ... ").append(IS_NULL(p));

                strInfo.append("\n");
                write(strInfo);
                return ret;
            }
        }

        return orig_sprintf(__s, __fmt, p);
    }
    //int printf(const char* __fmt, ...)
    HOOK_DEF(int, printf, const char *__fmt, char *p...) {
        DL_INFO
        IS_MATCH
                int ret = orig_printf(__fmt, p);
                auto strInfo = string("sprintf() arg1 -> ").append(IS_NULL(__fmt)).append(
                        "  ... ").append(
                        IS_NULL(p));
                strInfo.append("\n");
                write(strInfo);
                return ret;
            }
        }

        return orig_printf(__fmt, p);
    }
    //char *strtok(char *str, const char *delim)
    HOOK_DEF(char *, strtok, char *str, const char *delim) {

        DL_INFO
        IS_MATCH
                char *ret = orig_strtok(str, delim);
                auto strInfo = string("strcpy() arg1 -> ").append(IS_NULL(str)).append(
                        "  arg2-> ").append(IS_NULL(delim));
                if (ret != nullptr) {
                    strInfo.append("result -> ").append(IS_NULL(ret));
                }
                strInfo.append("\n");
                write(strInfo);
                return ret;
            }
        }

        return orig_strtok(str, delim);
    }
    //char* strdup(const char* __s);
    HOOK_DEF(char*, strdup, char *__s) {

        DL_INFO
        IS_MATCH
                auto strInfo = string("strdup() arg1 -> ").append(IS_NULL(__s));
                strInfo.append("\n");
                write(strInfo);
                return orig_strdup(__s);
            }
        }
        return orig_strdup(__s);
    }

    //ssize_t read(int __fd, void* __buf, size_t __count);
    HOOK_DEF(ssize_t, read, int __fd, void *__buf, size_t __count) {

        DL_INFO
        IS_MATCH
                ssize_t ret = orig_read(__fd, __buf, __count);
                if(ret==-1) {
                    return ret;
                }
                auto strInfo = string("read() arg2 -> ").append(IS_NULL((char *) __buf)).append(
                        " read count -> ").append(
                        to_string(__count));
                strInfo.append("\n");
                write(strInfo);
                return ret;

            }
        }
        return orig_read(__fd, __buf, __count);
    }
    //ssize_t write(int __fd, const void* __buf, size_t __count);
    HOOK_DEF(ssize_t, write, int __fd, void *__buf, size_t __count) {

        DL_INFO
        IS_MATCH
                ssize_t ret = orig_write(__fd, __buf, __count);
                if(ret==-1) {
                    return ret;
                }
                auto strInfo = string("write() arg2 -> ").append(IS_NULL((char *) __buf)).append(
                        " write count -> ").append(
                        to_string(__count));
                strInfo.append("\n");
                write(strInfo);
                return ret;

            }
        }
        return orig_write(__fd, __buf, __count);
    }
    //把 str1 和 str2 进行比较，结果取决于 LC_COLLATE 的位置设置。
    //int strcoll(const char* __lhs, const char* __rhs) __attribute_pure__;
    HOOK_DEF(int, strcoll, const char *__lhs, const char *__rhs) {
        DL_INFO
        IS_MATCH
                int ret = orig_strcoll(__lhs, __rhs);

                auto strInfo = string("strcoll() arg1 -> ").append(IS_NULL(__lhs)).append(
                        " arg2 -> ").append(IS_NULL(__rhs));
                strInfo.append("\n");
                write(strInfo);
                return ret;
            }
        }
        return orig_strcoll(__lhs, __rhs);
    }
    //size_t strxfrm(char *dest, const char *src, size_t n)
    HOOK_DEF(size_t, strxfrm, char *dest, const char *src, size_t n) {

        DL_INFO
        IS_MATCH
                size_t ret = orig_strxfrm(dest, src, n);
                auto strInfo = string("strxfrm() arg1 -> ").
                        append(IS_NULL(dest)).append(" arg2 -> ").append(IS_NULL(src));
                strInfo.append("\n");
                write(strInfo);
                return ret;
            }
        }
        return orig_strxfrm(dest, src, n);
    }
    //char* fgets(char* __buf, int __size, FILE* __fp);
    HOOK_DEF(char*, fgets, char *__buf, int __size, FILE *__fp) {
        DL_INFO
        IS_MATCH
                char *ret = orig_fgets(__buf, __size, __fp);
                auto strInfo = string("fgets() arg1 -> ").append(IS_NULL(__buf)).append(
                        "  arg2-> ").append(to_string(__size));
                if (ret != nullptr) {
                    strInfo.append("result -> ").append(IS_NULL(ret));
                }
                strInfo.append("\n");
                write(strInfo);
            }
        }
        return orig_fgets(__buf, __size, __fp);
    }
    //void *memcpy(void *destin, void *source, unsigned n);
    HOOK_DEF(void*, memcpy, void *destin, void *source, size_t __n) {
        DL_INFO
        IS_MATCH
        auto origStr = string("memcpy() call org destin -> [").append(IS_NULL((char *) source)).append("]\n");
        void *ret = orig_memcpy(destin, source, __n);
        auto strInfo =
                        string("arg1 -> [").append(IS_NULL((char *) destin)).append("] ").
                        append("arg2 -> [").append(IS_NULL((char *) source)).append("] ").
                        append(to_string(__n)).append("\n");
                char temp[__n*5];
                getpData(temp,source,__n);
                auto hexStr = string("source HEX-> ").append(temp);
                origStr.append(strInfo);
                origStr.append(temp);
                origStr.append("\n");
                write(origStr);
                return ret;
            }
        }

        return orig_memcpy(destin, source, __n);
    }
    //int snprintf(char* __buf, size_t __size, const char* __fmt, ...) __printflike(3, 4);
    HOOK_DEF(int, snprintf, char *__buf, size_t __size, const char *__fmt, char *p  ...) {
        DL_INFO
        IS_MATCH
                int ret = orig_snprintf(__buf, __size, __fmt, p);
                auto strInfo = string("snprintf() arg1 -> ").append(IS_NULL(__fmt)).append(
                        "  arg2-> ").append(IS_NULL(p));
                strInfo.append("\n");
                write(strInfo);
                return ret;
            }
        }

        return orig_snprintf(__buf, __size, __fmt, p);
    }
    //int vsnprintf(char* __buf, size_t __size, const char* __fmt, va_list __args) __printflike(3, 0);
    HOOK_DEF(int, vsnprintf, char *__buf, size_t __size, const char *__fmt, va_list __args) {
        DL_INFO
        IS_MATCH
                int ret = orig_vsnprintf(__buf, __size, __fmt, __args);

                auto strInfo = string("vsnprintf() arg1 -> ").append(IS_NULL(__fmt));
                strInfo.append("\n");
                write(strInfo);
                return ret;
            }
        }

        return orig_vsnprintf(__buf, __size, __fmt, __args);
    }
}


using namespace ZhenxiRunTime::stringHandlerHook;

void stringHandler::init() {
    void *handle = dlopen("libc.so", RTLD_NOW);

    if (handle == nullptr) {
        LOG(ERROR) << "strhadler get handle == null   ";
        return;
    }


    HOOK_SYMBOL_DOBBY(handle, strstr)
    HOOK_SYMBOL_DOBBY(handle, strcmp)
    HOOK_SYMBOL_DOBBY(handle, strcpy)
    HOOK_SYMBOL_DOBBY(handle, strdup)

    HOOK_SYMBOL_DOBBY(handle, strxfrm)
    HOOK_SYMBOL_DOBBY(handle, strtok)

//    HOOK_SYMBOL_DOBBY(handle, memcpy)
//    HOOK_SYMBOL_DOBBY(handle, read)
//    HOOK_SYMBOL_DOBBY(handle, write)

//    HOOK_SYMBOL_DOBBY(handle, sprintf);
//    HOOK_SYMBOL_DOBBY(handle, printf);
//    HOOK_SYMBOL_DOBBY(handle, snprintf);
//    HOOK_SYMBOL_DOBBY(handle, vsnprintf);

    //todo add hook string

    //strcat底层走的是strcpy
    //https://cs.android.com/android/platform/superproject/+/master:external/musl/src/string/strcat.c;l=5?q=strcat
//    HOOK_SYMBOL_DOBBY(handle, strcat);
//    HOOK_SYMBOL_DOBBY(handle, strlen);
//    HOOK_SYMBOL_DOBBY(handle, fgets);

//

    //strcoll底层是strcmp
    //https://cs.android.com/android/platform/superproject/+/master:external/musl/src/locale/strcoll.c;l=12;drc=master?q=strcoll&ss=android%2Fplatform%2Fsuperproject
//    HOOK_SYMBOL_DOBBY(handle, strcoll);



    dlclose(handle);

    LOG(ERROR) << ">>>>>>>>> string handler init success !  ";
}

void stringHandler::hookStrHandler(bool hookAll,
                                   const std::list<string> &forbid_list,
                                   const std::list<string> &filter_list,
                                   std::ofstream *os) {
    isHookAll = hookAll;
    filterSoList = std::list<string>(filter_list);
    forbidSoList = std::list<string>(forbid_list);

    if (os != nullptr) {
        isSave = true;
        hookStrHandlerOs = os;
    } else {
        LOG(ERROR) << ">>>>>>>>>>>>> string handler init fail hookStrHandlerOs == null  ";
    }
    init();
}

[[maybe_unused]]
void stringHandler::stopjnitrace() {
    filterSoList.clear();
    if (hookStrHandlerOs != nullptr) {
        if (hookStrHandlerOs->is_open()) {
            hookStrHandlerOs->close();
        }
        delete hookStrHandlerOs;
    }
    isSave = false;
}