#include "elf_util.h"
#include "dlfcn_nougat.h"

#include <jni.h>
#include <cstdio>
#include <cstring>
#include <cstdlib>
#include <sys/system_properties.h>
#include <cstdlib>
#include <dlfcn.h>
#include <android/log.h>


#include "ZhenxiLog.h"
#include "logging.h"



#ifndef DLFCN_COMPAT_zhenxi_H

#define DLFCN_COMPAT_zhenxi_H


#ifdef __cplusplus
extern "C" {
#endif

void *dlopen_compat(const char *filename, int flags);

void *dlsym_compat(void *handle, const char *symbol);

int dlclose_compat(void *handle);

const char *dlerror_compat();

void *getSymCompat(const char *filename, const char *symbol);

void* getSymByELF(const char * filename, const char *symbol);

#ifdef __cplusplus
}
#endif


#endif //DLFCN_COMPAT_zhenxi_H
