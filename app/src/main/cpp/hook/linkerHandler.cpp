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

#include "linkerHandler.h"
#include "logging.h"
#include "libpath.h"
#include "HookUtils.h"
#include "appUtils.h"



namespace ZhenxiRunTime::linkerHandler {
    static std::ofstream *hookStrHandlerOs;
    static bool isSave = false;
    
    void onSoLoadedAfter(const char *filename,void *ret){
        auto mapInfo = getSoBaseAddress(filename);
        char buffer[PATH_MAX];
        sprintf(buffer, "linker load %s  start-> 0x%zx  end-> 0x%zx  size -> %lu",
                filename, mapInfo.start, mapInfo.end, (mapInfo.end - mapInfo.start));
        if (isSave) {
            if (hookStrHandlerOs != nullptr) {
                (*hookStrHandlerOs) << buffer;
            }
        }
        LOGI("%s ", buffer);
    }
    HOOK_DEF(void *, dlopen_CI, const char *filename, int flag) {
        char temp[PATH_MAX];
        void *ret = orig_dlopen_CI(filename, flag);
        onSoLoadedAfter(filename, ret);
        return ret;
    }

    HOOK_DEF(void*, do_dlopen_CIV, const char *filename, int flag, const void *extinfo) {
        char temp[PATH_MAX];
        void *ret = orig_do_dlopen_CIV(filename, flag, extinfo);
        onSoLoadedAfter(filename, ret);
        return ret;
    }

    HOOK_DEF(void*, do_dlopen_CIVV, const char *name, int flags, const void *extinfo,
             void *caller_addr) {
        char temp[PATH_MAX];
        void *ret = orig_do_dlopen_CIVV(name, flags, extinfo, caller_addr);
        onSoLoadedAfter(name,ret);
        return ret;
    }

#define BREAK_FIND_SYSCALL 0
#define CONTINUE_FIND_SYSCALL 1

#if defined(__arm__)
    typedef Elf32_Word elf_word;
typedef Elf32_Half elf_half;
typedef Elf32_Ehdr elf_ehdr;
typedef Elf32_Shdr elf_shdr;
typedef Elf32_Sym elf_sym;
#elif defined(__i386__)
    typedef Elf32_Word elf_word;
typedef Elf32_Half elf_half;
typedef Elf32_Ehdr elf_ehdr;
typedef Elf32_Shdr elf_shdr;
typedef Elf32_Sym elf_sym;
#elif defined(__aarch64__)
    typedef Elf64_Word elf_word;
    typedef Elf64_Half elf_half;
    typedef Elf64_Ehdr elf_ehdr;
    typedef Elf64_Shdr elf_shdr;
    typedef Elf64_Sym elf_sym;
#else
#error "Unsupported architecture"
#endif
    
    
    static ssize_t read_strtab(FILE *fp, elf_shdr *shdr, char **datap) {
        elf_word sh_size;
        long cur_off;
        char *data;


        sh_size = shdr->sh_size;

        if ((size_t) sh_size > SIZE_MAX - 1) {
            fprintf(stderr, "read_strtab: %s", strerror(EFBIG));
            goto _ret;
        }


        cur_off = ftell(fp);

        if (fseek(fp, shdr->sh_offset, SEEK_SET) != 0) {
            perror("read_strtab: fseek");
            goto _ret;
        }

        if ((data = (char *) malloc(sh_size + 1)) == NULL) {
            perror("read_strtab: malloc");
            goto _ret;
        }

        if (fread(data, 1, sh_size, fp) != sh_size) {
            perror("read_strtab: fread");
            goto _free;
        }

        data[sh_size] = 0;

        if (fseek(fp, cur_off, SEEK_SET) != 0) {
            perror("read_strtab: fseek");
            goto _free;
        }

        *datap = data;

        return (ssize_t) sh_size;

        _free:
        free(data);

        _ret:
        return -1;
    }
    static int resolve_symbol_from_symtab(FILE *fp, elf_shdr *symtab, char *strtab,
                                          size_t strtab_size, const char *symname, intptr_t *symval) {
        elf_word i, num_syms;
        elf_sym sym;
        long cur_off;

        int r = -1;

        cur_off = ftell(fp);

        if (fseek(fp, symtab->sh_offset, SEEK_SET) != 0) {
            perror("resolve_symbol_from_symtab: fseek");
            goto _ret;
        }

        num_syms = symtab->sh_size / sizeof(elf_sym);

        for (i = 0; i < num_syms; i++) {
            if (fread(&sym, sizeof(elf_sym), 1, fp) != 1) {
                perror("resolve_symbol_from_symtab: fread");
                goto _ret;
            }

            if (sym.st_name < strtab_size &&
                strcmp(&strtab[sym.st_name], symname) == 0) {
                *symval = sym.st_value;
                break;
            }
        }

        if (fseek(fp, cur_off, SEEK_SET) != 0) {
            perror("resolve_symbol_from_symtab: fseek");
            goto _ret;
        }

        if (i < num_syms)
            r = 0;

        _ret:
        return r;
    }
    
    static int resolve_symbol_from_sections(FILE *fp, elf_shdr *shdrs,
                                            elf_half num_sects, const char *symname, intptr_t *symval) {
        elf_half i;
        elf_shdr *shdr, *strtab_shdr;
        char *strtab;
        ssize_t strtab_size;

        int r = -1;

        for (i = 0; i < num_sects; i++) {
            shdr = &shdrs[i];

            if (shdr->sh_type == SHT_SYMTAB && shdr->sh_link < num_sects) {
                strtab_shdr = &shdrs[shdr->sh_link];

                if ((strtab_size = read_strtab(fp, strtab_shdr, &strtab)) < 0)
                    goto _ret;

                r = resolve_symbol_from_symtab(fp, shdr, strtab, (size_t) strtab_size,
                                               symname, symval);

                free(strtab);

                if (r == 0)
                    goto _ret;
            }

        }

        _ret:
        return r;
    }
    
    int resolve_symbol(const char *filename, const char *symname, intptr_t *symval) {
        FILE *fp;
        elf_ehdr ehdr;
        elf_shdr *shdrs;
        elf_half shnum;

        int r = -1;

        if ((fp = fopen(filename, "r")) == NULL) {
            perror("resolve_symbol: fopen");
            goto _ret;
        }

        if (fread(&ehdr, sizeof(ehdr), 1, fp) != 1) {
            perror("resolve_symbol: fread");
            goto _close;
        }

        if (fseek(fp, ehdr.e_shoff, SEEK_SET) != 0) {
            perror("resolve_symbol: fseek");
            goto _close;
        }

        shnum = ehdr.e_shnum;

        if ((shdrs = (elf_shdr *) (calloc(shnum, sizeof(elf_shdr)))) == NULL) {
            perror("resolve_symbol: calloc");
            goto _close;
        }

        if (fread(shdrs, sizeof(elf_shdr), shnum, fp) != shnum) {
            perror("resolve_symbol: fread");
            goto _free;
        }

        r = resolve_symbol_from_sections(fp, shdrs, shnum, symname, symval);

        _free:
        free(shdrs);

        _close:
        fclose(fp);

        _ret:
        return r;
    }
    intptr_t get_addr(const char *name) {
        char buf[BUFSIZ], *tok[6];
        int i;
        FILE *fp;
        intptr_t r = NULL;

        //暂不替换,因为这个在io重定向之前执行,所以正常传入maps即可
        fp = fopen("/proc/self/maps", "r");
        if(fp == NULL) {
            perror("get_linker_addr: fopen");
            goto ret;
        }


        while (fgets(buf, sizeof(buf), fp)) {
            i = strlen(buf);
            //LOGE("get linker maps item -> %s ",buf);
            if (i > 0 && buf[i - 1] == '\n')
                buf[i - 1] = 0;

            tok[0] = strtok(buf, " ");
            for (i = 1; i < 6; i++)
                tok[i] = strtok(NULL, " ");

            if (tok[5] && strcmp(tok[5], name) == 0) {
                r = (intptr_t) strtoul(tok[0], NULL, 16);
                goto close;
            }
        }

        close:
        fclose(fp);

        ret:
        return r;
    }

    bool relocate_linker(const char *linker_path){
        intptr_t linker_addr, dlopen_off, symbol;
        if ((linker_addr = get_addr(linker_path)) == 0) {
            ALOGE("cannot found linker addr  %s", linker_path)
            return false;
        }
        if (resolve_symbol(linker_path, "__dl__Z9do_dlopenPKciPK17android_dlextinfoPKv",
                           &dlopen_off) == 0) {
            symbol = linker_addr + dlopen_off;
            HookUtils::Hooker((void *) symbol, (void *) new_do_dlopen_CIVV,
                              (void **) &orig_do_dlopen_CIVV);
            return true;
        } else if (resolve_symbol(linker_path, "__dl__Z9do_dlopenPKciPK17android_dlextinfoPv",
                                  &dlopen_off) == 0) {
            symbol = linker_addr + dlopen_off;
            HookUtils::Hooker((void *) symbol, (void *) new_do_dlopen_CIVV,
                              (void **) &orig_do_dlopen_CIVV);
            return true;
        } else if (resolve_symbol(linker_path, "__dl__ZL10dlopen_extPKciPK17android_dlextinfoPv",
                                  &dlopen_off) == 0) {
            symbol = linker_addr + dlopen_off;
            HookUtils::Hooker((void *) symbol, (void *) new_do_dlopen_CIVV,
                              (void **) &orig_do_dlopen_CIVV);
            return true;
        } else if (
                resolve_symbol(linker_path, "__dl__Z20__android_dlopen_extPKciPK17android_dlextinfoPKv",
                               &dlopen_off) == 0) {
            symbol = linker_addr + dlopen_off;
            HookUtils::Hooker((void *) symbol, (void *) new_do_dlopen_CIVV,
                              (void **) &orig_do_dlopen_CIVV);
            return true;
        } else if (
                resolve_symbol(linker_path, "__dl___loader_android_dlopen_ext",
                               &dlopen_off) == 0) {
            symbol = linker_addr + dlopen_off;
            HookUtils::Hooker((void *) symbol, (void *) new_do_dlopen_CIVV,
                              (void **) &orig_do_dlopen_CIVV);
            return true;
        } else if (resolve_symbol(linker_path, "__dl__Z9do_dlopenPKciPK17android_dlextinfo",
                                  &dlopen_off) == 0) {
            symbol = linker_addr + dlopen_off;
            HookUtils::Hooker((void *) symbol, (void *) new_do_dlopen_CIV,
                              (void **) &orig_do_dlopen_CIV);
            return true;
        } else if (resolve_symbol(linker_path, "__dl__Z8__dlopenPKciPKv",
                                  &dlopen_off) == 0) {
            symbol = linker_addr + dlopen_off;
            HookUtils::Hooker((void *) symbol, (void *) new_do_dlopen_CIV,
                              (void **) &orig_do_dlopen_CIV);
            return true;
        } else if (resolve_symbol(linker_path, "__dl___loader_dlopen",
                                  &dlopen_off) == 0) {
            symbol = linker_addr + dlopen_off;
            HookUtils::Hooker((void *) symbol, (void *) new_do_dlopen_CIV,
                              (void **) &orig_do_dlopen_CIV);
            return true;
        } else if (resolve_symbol(linker_path, "__dl_dlopen",
                                  &dlopen_off) == 0) {
            symbol = linker_addr + dlopen_off;
            HookUtils::Hooker((void *) symbol, (void *) new_dlopen_CI,
                              (void **) &orig_dlopen_CI);
            return true;
        }
        return false;
    }

}

using namespace ZhenxiRunTime::linkerHandler;


static void init() {

    bool isSuccess = relocate_linker(getLinkerPath());

    LOG(ERROR) << ">>>>>>>>> linker handler init  !  "<<(isSuccess?"true":"false");
}

void linkerHandler::linkerCallBack(std::ofstream *os) {
    if (os != nullptr) {
        isSave = true;
        hookStrHandlerOs = os;
    } else {
        LOG(ERROR) << ">>>>>>>>>>>>> string handler init fail hookStrHandlerOs == null  ";
    }
    init();
}

[[maybe_unused]]
void linkerHandler::stopjnitrace() {
    if (hookStrHandlerOs != nullptr) {
        if (hookStrHandlerOs->is_open()) {
            hookStrHandlerOs->close();
        }
        delete hookStrHandlerOs;
    }
    isSave = false;
}