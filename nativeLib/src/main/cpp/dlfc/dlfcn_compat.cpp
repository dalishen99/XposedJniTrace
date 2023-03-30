#include "../includes/dlfcn_nougat.h"

#include <sys/system_properties.h>
#include <cstdlib>
#include <dlfcn.h>

#include "../includes/dlfcn_compat.h"
#include "../includes/logging.h"
#include "../includes/elf_util.h"



int dlclose_compat(void *handle) {
    if(handle== nullptr){
        return 1;
    }
    if (get_sdk_level() >= 24) {
        return fake_dlclose(handle);
    } else {
        return dlclose(handle);
    }
}

void *dlopen_compat(const char *filename, int flags) {
    if(filename== nullptr){
        return nullptr;
    }
    if (get_sdk_level() >= 24) {
        return fake_dlopen(filename, flags);
    } else {
        return dlopen(filename, flags);
    }
}

void *dlsym_compat(void *handle, const char *symbol) {

    void* resut = nullptr;

    if (get_sdk_level() >= 24) {
        resut =  fake_dlsym(handle, symbol);
    } else {
        resut =  dlsym(handle, symbol);
    }
    return resut;
}

const char *dlerror_compat() {
    if (get_sdk_level() >= 24) {
        return fake_dlerror();
    } else {
        return dlerror();
    }
}

void* getSymByELF(const char * filename, const char *symbol){
    //尝试解析Debug符号,栈里面,自动释放
    SandHook::ElfImg elfImg(filename);
    void *pSymbol = elfImg.getSymbAddress(symbol);
    if(pSymbol!= nullptr){
        //LOGI("get debug symbol sucess! ")
        return pSymbol;
    } else{
        LOGE("get debug symbol fail ! %s %s ",filename,symbol)
        return nullptr;
    }
}
void* getSymCompat(const char * filename, const char *symbol){
    void *phandle = dlopen_compat(filename, RTLD_NOW);
    if(phandle==nullptr){
        LOGE("getSymCompat get handle error  %s \n %s ",filename,symbol)
        return nullptr;
    }

    void *pSymbol = dlsym_compat(phandle, symbol);
    if(pSymbol!=nullptr){
        return pSymbol;
    }else{
        //LOGE("getSymCompat get sym error , start get debug symbol  %s ",symbol)
    }
    return getSymByELF(filename,symbol);
}




