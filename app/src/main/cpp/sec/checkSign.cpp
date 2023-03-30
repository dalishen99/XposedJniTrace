//
// Created by Zhenxi on 2021/5/10.
//

#include "cert.h"
#include "Md5Utils.h"
#include "logging.h"


#define JNINativeMethodSize   sizeof(JNINativeMethod)

void checkApkSign(JNIEnv *env, jobject thiz, jobject context) {
    const string &basicString = Md5Utils::MD5(checkSign(env, context));
    //LOG(ERROR) << "apk sign md5 info  "<<basicString;
    if(basicString!= "43d7c3de4793b2b46c2977863f41e122"){
        jclass SysClazz = env->FindClass("java/lang/System");
        jmethodID exit_id = env->GetStaticMethodID(SysClazz, "exit", "(I)V");
        env->CallStaticVoidMethod(SysClazz,exit_id,9);
    }
}

static JNINativeMethod gMethods[] = {
        {"AppSecure", "(Landroid/content/Context;)V", (void *)checkApkSign}
};

jint JNICALL JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env = nullptr;
    if (vm->GetEnv((void **) &env, JNI_VERSION_1_6) == JNI_OK) {
        auto MainClass = env->FindClass("com/zhenxi/jnitrace/App");
        if (env->RegisterNatives(MainClass,
                                 gMethods, sizeof(gMethods) /JNINativeMethodSize) < 0) {
            return JNI_ERR;
        }
        return JNI_VERSION_1_6;
    }
    return JNI_ERR;
}

