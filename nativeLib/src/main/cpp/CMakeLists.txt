cmake_minimum_required(VERSION 3.4.1)

ENABLE_LANGUAGE(ASM)



include_directories(
        ./includes/
)

add_subdirectory(adapter)

add_subdirectory(dlfc)
add_subdirectory(libpath)
add_subdirectory(utils)
add_subdirectory(hook)
add_subdirectory(raw_syscall)






