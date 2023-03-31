cmake_minimum_required(VERSION 3.4.1)
enable_language(C ASM)
project(XposedJniTrace)

SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wno-narrowing -std=c++20")

string(REGEX REPLACE "(.*)/app/src.*" "\\1" HomePath ${PROJECT_SOURCE_DIR})

message(STATUS "funJni building ${PROJECT_SO_NAME}  home path -> ${HomePath} ")


add_definitions(-DCORE_SO_NAME="lib${PROJECT_SO_NAME}.so")


find_library(
        log-lib
        log
)


add_subdirectory(${HomePath}/nativeLib/src/main/cpp/ nativeLib.out)

add_subdirectory(sec)

include_directories(
        ./
        ./includes/
        ${HomePath}/nativeLib/src/main/cpp/
        ${HomePath}/nativeLib/src/main/cpp/includes/
)


if (ZHENXI_BUILD_TYPE STREQUAL "RELEASE")
    SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fvisibility=hidden -fvisibility-inlines-hidden")
    SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -s")
    SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Os")
    SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -ffunction-sections -fdata-sections")
    SET(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fomit-frame-pointer")

    add_definitions(-DZHENXI_BUILD_TYPE_NOLOG)
    add_definitions(-DZHENXI_BUILD_TYPE_RELEASE)
endif ()


aux_source_directory(hook hook_src_list)

add_library(
        ${PROJECT_SO_NAME}

        SHARED

        main.cpp

        ${hook_src_list}
)

target_link_libraries(
        ${PROJECT_SO_NAME}
        ${log-lib}
)

#link native lib
target_link_libraries(
        ${PROJECT_SO_NAME}

        adapter
        dlfc
        runtimehook
        libpath

        utils
        raw_syscall
)


