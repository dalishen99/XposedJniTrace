//
// Created by zhenxi on 2022/2/6.
//

#ifndef QCONTAINER_PRO_INVOKEPRINTF_H
#define QCONTAINER_PRO_INVOKEPRINTF_H

#include "AllInclude.h"

class invokePrintf {
public:
    static void HookJNIInvoke(JNIEnv *env,std::ofstream *os,
                                            std::string(*prettyMethodSym)(void *,bool));
    static void HookJNIRegisterNative(JNIEnv *env,
                                             std::ofstream *os,
                                             std::string(*prettyMethodSym)(void *,bool));
};


#endif //QCONTAINER_PRO_INVOKEPRINTF_H
