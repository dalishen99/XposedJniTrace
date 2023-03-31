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
        jboolean isListenerAll , jobject jmap, jstring filepath) {

    LOG(INFO) << "<<<<<<<<< startHookJni start init >>>>>>>>>>  " ;

    const auto &filter_list = parse::jlist2clist(env, jmap);
    auto listenerAll = parse::jboolean2bool(isListenerAll);

    auto prettyMethodSym =
            reinterpret_cast<std::string(*)(void *, bool)>
                    (fake_dlsym(fake_dlopen(getlibArtPath(), RTLD_NOW),
                                "_ZN3art9ArtMethod12PrettyMethodEb"));
    //排除我们自己的SO,防止重复调用导致栈溢出。
    const std::list<string> forbid_list {CORE_SO_NAME};
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
        //hook jni
        Jnitrace::startjnitrace(env,listenerAll, forbid_list,filter_list, saveOs);
        //hook libc string handle function
        //stringHandler::hookStrHandler(listenerAll, forbid_list,filter_list, saveOs);

        //hook so linker
        //linkerHandler::linkerCallBack(saveOs);
        //hook jni register native
        //invokePrintf::HookJNIRegisterNative(env,saveOs,prettyMethodSym);
        //hook all java invoke
        //invokePrintf::HookJNIInvoke(env,saveOs,prettyMethodSym);
    } else {
        //现阶段一定会保存到文件里面
        //Jnitrace::startjnitrace(env, listenerAll, forbid_list,filter_list, nullptr);
        //stringHandler::hookStrHandler(listenerAll, forbid_list,filter_list, nullptr);
        //linkerHandler::linkerCallBack(nullptr);
        //invokePrintf::HookJNIRegisterNative(env, nullptr,prettyMethodSym);
        //invokePrintf::HookJNIInvoke(env,nullptr,prettyMethodSym);
    }
    LOG(INFO) << ">>>>>>>>>>>>> jni hook finish  !  " ;
}

static JNINativeMethod gMethods[] = {
        {"startHookJni", "(ZLjava/util/ArrayList;Ljava/lang/String;)V", (void *)startHookJni},
};

jint JNICALL
JNI_OnLoad(JavaVM *vm, void *reserved) {
    LOG(INFO) << "FunJni  JNI_OnLoad start ";
    mVm = vm;
    JNIEnv *env = nullptr;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) == JNI_OK) {
        mEnv = env;
        auto MainClass = (jclass) env->FindClass("com/zhenxi/jnitrace/LHook");
        if (env->RegisterNatives(MainClass, gMethods,
                                 sizeof(gMethods) / sizeof(gMethods[0]))<0) {
            return JNI_ERR;
        }
        LOG(ERROR) << ">>>>>>>>>>>> FunJni JNI_OnLoad load success";
        return JNI_VERSION_1_6;
    }
    LOG(ERROR) << "FunJni  JNI_OnLoad load fail ";
    return JNI_ERR;
}
