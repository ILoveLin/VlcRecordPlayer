cmake_minimum_required(VERSION 3.4.1)

#set(CMAKE_C_STANDARD 99)
#添加动态库，包含源文件路径
#传递三个参数，第一个参数是模块名称，第二个参数是SHARED或者STATIC。如果是源文件模块，第三个参数是源文件列表；
#如果是第三方库，第三个参数是IMPORTED。
add_library(yylRecord
        SHARED
        src/main/cpp/yyl_record.c
        src/main/cpp/yyl_log.c
        #   src/main/cpp/yyl_fixsurface.c
        )

#添加动态库，包含源文件路径
add_library(vlc
        SHARED
        IMPORTED
        )
#添加动态库，包含源文件路径
add_library(vlcjni
        SHARED
        IMPORTED
        )
message(AUTHOR_WARNING 'yyl CMAKE_ANDROID_ARCH_ABI=${CMAKE_ANDROID_ARCH_ABI})
message(AUTHOR_WARNING 'yyl CMAKE_SOURCE_DIR=${CMAKE_SOURCE_DIR})

set(soPath ${CMAKE_SOURCE_DIR}/jniLibs/${CMAKE_ANDROID_ARCH_ABI}/libvlc.so)
set(soPathjni ${CMAKE_SOURCE_DIR}/jniLibs/${CMAKE_ANDROID_ARCH_ABI}/libvlcjni.so)


#message(AUTHOR_WARNING " jnilib=${soPathjni}")
#指定第三方库路径
#用于指定第三方库路径，IMPORT_LOCATION一般是指向src/main/cpp目录
set_target_properties(
        vlc
        PROPERTIES IMPORTED_LOCATION
        ${soPath}
)
#指定第三方库路径
#用于指定第三方库路径，IMPORT_LOCATION一般是指向src/main/cpp目录
set_target_properties(
        vlcjni
        PROPERTIES IMPORTED_LOCATION
        ${soPathjni}
)
#指定头文件路径
include_directories(
        ${CMAKE_SOURCE_DIR}/include/
)

#链接目标库
#链接目标库，把依赖库都链接到目标库中
target_link_libraries(yylRecord
        android
        log
        vlc
        vlcjni
        )
