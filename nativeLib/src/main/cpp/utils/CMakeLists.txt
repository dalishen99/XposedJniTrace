cmake_minimum_required(VERSION 3.4.1)
#SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wno-narrowing -std=c++20")
#添加内联支持
enable_language(C ASM)






#native基础类库路径
include_directories(
        ./
        ${HomePath}/nativeLib/src/main/cpp/
        ${HomePath}/nativeLib/src/main/cpp/includes/
)








#这个库主要是包含一些常用的工具类
add_library(
        utils

        STATIC

        parse.cpp
        fileUtils.cpp
        stringUtils.cpp
        appUtils.cpp
)

target_link_libraries(
        utils

        dlfc
        libpath
        runtimehook
        ${log-lib}
)




find_library(
        log-lib
        log
)






