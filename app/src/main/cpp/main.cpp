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




void startHookJni(JNIEnv *env, jclass clazz,
        jobject jmap, jstring filepath) {
    const auto &cList = parse::jlist2clist(env, jmap);
    string path;

    auto prettyMethodSym =
            reinterpret_cast<std::string(*)(void *, bool)>
                    (fake_dlsym(fake_dlopen(getlibArtPath(), RTLD_NOW),
                                "_ZN3art9ArtMethod12PrettyMethodEb"));

    if (filepath != nullptr) {
        path = parse::jstring2str(env, filepath);
        auto *saveOs = new ofstream();
        saveOs->open(path, ios::app);
        if (!saveOs->is_open()) {
            LOG(INFO) << "startHookJni open file error  " << path;
            LOG(INFO) << "startHookJni open file error  " << path;
            LOG(INFO) << "startHookJni open file error  " << path;
            return;
        }
        //hook jni
        Jnitrace::startjnitrace(env, cList, saveOs);
        //hook libc string handle function
        stringHandler::hookStrHandler(cList, saveOs);
        //hook so linker
        linkerHandler::linkerCallBack(saveOs);
        //hook jni register native
        invokePrintf::HookJNIRegisterNative(env,saveOs,prettyMethodSym);
        //hook all java invoke
        //invokePrintf::HookJNIInvoke(env,saveOs,prettyMethodSym);

    } else {
        Jnitrace::startjnitrace(env, cList, nullptr);
        stringHandler::hookStrHandler(cList, nullptr);
        linkerHandler::linkerCallBack(nullptr);
        invokePrintf::HookJNIRegisterNative(env, nullptr,prettyMethodSym);
        //invokePrintf::HookJNIInvoke(env,nullptr,prettyMethodSym);
    }
}

static JNINativeMethod gMethods[] = {
        {"startHookJni", "(Ljava/util/ArrayList;Ljava/lang/String;)V", (void *)startHookJni},
};

jint JNICALL
JNI_OnLoad(JavaVM *vm, void *reserved) {
    LOG(ERROR) << "FunJni  JNI_OnLoad start ";
    JNIEnv *env = nullptr;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) == JNI_OK) {
        mEnv = env;
        auto MainClass = (jclass) env->FindClass("com/zhenxi/jnitrace/Hook/LHook");
        if (env->RegisterNatives(MainClass, gMethods,
                                 sizeof(gMethods) / sizeof(gMethods[0]))<0) {
            return JNI_ERR;
        }
        LOG(ERROR) << "FunJni JNI_OnLoad load sucess";
        return JNI_VERSION_1_6;
    }
    LOG(ERROR) << "FunJni  JNI_OnLoad load fail ";
    return JNI_ERR;
}
