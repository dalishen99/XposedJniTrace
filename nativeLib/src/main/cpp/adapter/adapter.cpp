//
// Created by Zhenxi on 2022/10/17.
//


#include "adapter.h"

JavaVM *mVm;
JNIEnv *mEnv;


static int SDK_INT = -1;

int get_sdk_level() {
    if (SDK_INT > 0) {
        return SDK_INT;
    }
    char sdk[PROP_VALUE_MAX] = {0};
    __system_property_get("ro.build.version.sdk", sdk);
    SDK_INT = atoi(sdk);
    return SDK_INT;
}




ScopeUtfString::ScopeUtfString(jstring j_str) {
    _j_str = j_str;
    _c_str = getRunTimeEnv()->GetStringUTFChars(j_str, nullptr);
}

ScopeUtfString::~ScopeUtfString() {
    getRunTimeEnv()->ReleaseStringUTFChars(_j_str, _c_str);
}

JNIEnv *getRunTimeEnv() {
    //一个进程一个env
    //JNIEnv *env;
    if (mEnv == nullptr) {
        mVm->GetEnv(reinterpret_cast<void **>(&mEnv), JNI_VERSION_1_6);
    }
    return mEnv;
}

JNIEnv *ensureEnvCreated() {
    JNIEnv *env = getRunTimeEnv();
    if (env == nullptr) {
        mVm->AttachCurrentThread(&env, nullptr);
    }
    return env;
}


void DetachCurrentThread() {
    mVm->DetachCurrentThread();
}