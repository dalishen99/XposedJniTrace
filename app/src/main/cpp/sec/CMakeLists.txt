cmake_minimum_required(VERSION 3.4.1)
enable_language(C ASM)


include_directories(
        ./
        ${HomePath}/nativeLib/src/main/cpp/
        ${HomePath}/nativeLib/src/main/cpp/includes/
)

add_library(
        secure

        SHARED

        checkSign.cpp
        Md5Utils.cpp
        cert.cpp
)


target_link_libraries(
        secure
        ${log-lib}
)

target_link_libraries(
        secure

        raw_syscall
)