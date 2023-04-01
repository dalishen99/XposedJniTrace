#include <libgen.h>
#include <jni.h>
#include <logging.h>
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

#include "libpath.h"
#include "parse.h"
#include "JnitraceForC.h"
#include "stringHandler.h"
#include "invokePrintf.h"
#include "linkerHandler.h"


#define PRINTF_LIST(list) \
    do { \
        std::stringstream ss; \
        ss << #list << ": ["; \
        for (const auto &item : list) { \
            ss << item << " "; \
        } \
        ss << "]"; \
        LOGE("native printf -> %s", ss.str().c_str()); \
    } while (0)


static inline bool has_boolean(const std::list<string> &str_list, const std::string &target) {
    for (const auto &str: str_list) {
        if (str == target) {
            return true;
        }
    }
    return false;
}

std::list<string> jlist2clist(JNIEnv *env, jobject jlist) {
    std::list<std::string> clist;
    jclass listClazz = env->FindClass("java/util/ArrayList");
    jmethodID sizeMid = env->GetMethodID(listClazz, "size", "()I");
    jint size = env->CallIntMethod(jlist, sizeMid);
    jmethodID list_get = env->GetMethodID(listClazz, "get", "(I)Ljava/lang/Object;");
    for (int i = 0; i < size; i++) {
        jobject item = env->CallObjectMethod(jlist, list_get, i);
        clist.push_back(parse::jstring2str(env, (jstring) item));
    }
    return clist;
}

void startHookJni(JNIEnv *env,
                  [[maybe_unused]] jclass clazz,
                  jboolean isListenerAll,
                  jobject jFilterList,
                  jobject jFunctionList,
                  jstring filepath) {

    LOG(INFO) << "<<<<<<<<< startHookJni start init >>>>>>>>>>  ";

    const auto &filter_list = jlist2clist(env, jFilterList);
    PRINTF_LIST(filter_list);
    const auto &function_list = jlist2clist(env, jFunctionList);
    PRINTF_LIST(function_list);

    auto listenerAll = parse::jboolean2bool(isListenerAll);
    auto prettyMethodSym =
            reinterpret_cast<std::string(*)(void *, bool)>
            (fake_dlsym(fake_dlopen(getlibArtPath(), RTLD_NOW),
                        "_ZN3art9ArtMethod12PrettyMethodEb"));
    //排除我们自己的SO,防止重复调用导致栈溢出。
    const std::list<string> forbid_list{CORE_SO_NAME};
    if (filepath != nullptr) {
        auto path = parse::jstring2str(env, filepath);
        auto *saveOs = new ofstream();
        saveOs->open(path, ios::app);
        if (!saveOs->is_open()) {
            LOG(INFO) << "startHookJni open file error  " << path;
            LOG(INFO) << "startHookJni open file error  " << path;
            LOG(INFO) << "startHookJni open file error  " << path;
            return;
        }
        if (has_boolean(function_list, "0")) {
            //hook jni
            Jnitrace::startjnitrace(env, listenerAll, forbid_list, filter_list, saveOs);
        }
        if (has_boolean(function_list, "1")) {
            //hook libc string handle function
            stringHandler::hookStrHandler(listenerAll, forbid_list, filter_list, saveOs);
        }
        if (has_boolean(function_list, "2")) {
            //hook jni register native
            invokePrintf::HookJNIRegisterNative(env, saveOs, prettyMethodSym);
        }
        if (has_boolean(function_list, "3")) {
            //hook so linker
            linkerHandler::linkerCallBack(saveOs);
        }
        if (has_boolean(function_list, "4")) {
            //hook all java invoke
            invokePrintf::HookJNIInvoke(env, saveOs, prettyMethodSym);
        }
    }
    else {
        //现阶段一定会保存到文件里面,下面的逻辑可能不会执行
//        if (has_boolean(function_list, 0)) {
//            //hook jni
//            Jnitrace::startjnitrace(env, listenerAll, forbid_list, filter_list, nullptr);
//        }
//        if (has_boolean(function_list, 1)) {
//            //hook libc string handle function
//            stringHandler::hookStrHandler(listenerAll, forbid_list, filter_list, nullptr);
//        }
//        if (has_boolean(function_list, 2)) {
//            //hook jni register native
//            invokePrintf::HookJNIRegisterNative(env, nullptr, prettyMethodSym);
//        }
//        if (has_boolean(function_list, 3)) {
//            //hook so linker
//            linkerHandler::linkerCallBack(nullptr);
//        }
//        if (has_boolean(function_list, 4)) {
//            //hook all java invoke
//            invokePrintf::HookJNIInvoke(env,nullptr,prettyMethodSym);
//        }
    }
    LOG(INFO) << ">>>>>>>>>>>>> jni hook finish  !  ";
}

static JNINativeMethod gMethods[] = {
        {"startHookJni", "(ZLjava/util/ArrayList;Ljava/util/ArrayList;Ljava/lang/String;)V",
         (void *) startHookJni},
};

jint JNICALL
JNI_OnLoad(JavaVM *vm, [[maybe_unused]] void *reserved) {
    LOG(INFO) << "FunJni  JNI_OnLoad start ";
    mVm = vm;
    JNIEnv *env = nullptr;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) == JNI_OK) {
        mEnv = env;
        auto MainClass = (jclass) env->FindClass("com/zhenxi/jnitrace/LHook");
        if (env->RegisterNatives(MainClass, gMethods,
                                 sizeof(gMethods) / sizeof(gMethods[0])) < 0) {
            return JNI_ERR;
        }
        LOG(ERROR) << ">>>>>>>>>>>> FunJni JNI_OnLoad load success";
        return JNI_VERSION_1_6;
    }
    LOG(ERROR) << "FunJni  JNI_OnLoad load fail ";
    return JNI_ERR;
}
