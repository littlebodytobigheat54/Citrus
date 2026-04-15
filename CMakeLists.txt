cmake_minimum_required(VERSION 3.22)
project(CitraAndroid CXX C ASM)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

# ── Build Options ──────────────────────────────────────────────
option(ENABLE_VULKAN    "Enable Vulkan renderer"        ON)
option(ENABLE_OPENGL    "Enable OpenGL ES renderer"     ON)
option(ENABLE_JIT       "Enable ARM JIT (dynarmic)"     ON)
option(ENABLE_DISCORD   "Enable Discord Rich Presence"  OFF)
option(ENABLE_FFMPEG    "Enable FFmpeg for media"       OFF)

# ── Android-specific flags ─────────────────────────────────────
if(ANDROID)
    set(CMAKE_ANDROID_API_MIN 26)

    # ARM64 optimizations
    if(ANDROID_ABI STREQUAL "arm64-v8a")
        set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} \
            -march=armv8-a+fp+simd+crypto+crc \
            -mtune=cortex-a76 \
            -O3 \
            -ffast-math \
            -fno-strict-aliasing")
        add_definitions(-DARCH_ARM64)
    endif()

    # Link Android system libraries
    find_library(LOG_LIB        log)
    find_library(ANDROID_LIB    android)
    find_library(EGL_LIB        EGL)
    find_library(GLES3_LIB      GLESv3)
    find_library(AAUDIO_LIB     aaudio)
    find_library(OPENSLES_LIB   OpenSLES)
    find_library(ZLIB_LIB       z)
endif()

# ── Compiler Warnings ──────────────────────────────────────────
if(CMAKE_CXX_COMPILER_ID MATCHES "Clang|GNU")
    add_compile_options(
        -Wall -Wextra -Wpedantic
        -Wno-unused-parameter
        -Wno-missing-field-initializers
    )
endif()

# ── Subdirectories ─────────────────────────────────────────────
add_subdirectory(android/app/src/main/cpp)
