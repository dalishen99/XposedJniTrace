//
// Created by zhenxi on 2021/5/16.
//


#include "logging.h"
#include "dlfcn_compat.h"




#ifndef VMP_HOOKUTILS_H
#define VMP_HOOKUTILS_H

#define HOOK_DEF(ret, func, ...) \
  ret (*orig_##func)(__VA_ARGS__)=nullptr; \
  ret new_##func(__VA_ARGS__)

class HookUtils {
public:

    static bool Hooker(void *dysym, void *repl, void **org);

    static bool Hooker(void *handler, const char *dysym, void *repl, void **org);

    static bool Hooker(void *dysym, void *repl, void **org, const char *dynSymName);

    static bool Hooker(const char *libName, const char *dysym, void *repl, void **org);

    static bool unHook(void *sym);

};

void hook_libc_function(void *handle, const char *symbol, void *new_func, void **old_func) ;

#endif //VMP_HOOKUTILS_H
