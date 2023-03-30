cmake_minimum_required(VERSION 3.4.1)

#SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wno-narrowing -std=c++20")
#添加内联支持
enable_language(C ASM)








add_library(
        dlfc

        STATIC

        dlfcn_compat.cpp
        dlfcn_nougat.cpp
        elf_util.cpp

)

target_link_libraries(
        dlfc
        adapter
        ${log-lib}
)


find_library(
        log-lib
        log
)








