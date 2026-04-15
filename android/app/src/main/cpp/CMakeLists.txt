cmake_minimum_required(VERSION 3.22)

# ── Citra Core Source ──────────────────────────────────────────
set(CITRA_SRC ${CMAKE_CURRENT_SOURCE_DIR}/citra-src)

# ── Collect all Citra source files ────────────────────────────
file(GLOB_RECURSE CITRA_CORE_SOURCES
    "${CITRA_SRC}/core/*.cpp"
    "${CITRA_SRC}/core/*.h"
    "${CITRA_SRC}/video_core/*.cpp"
    "${CITRA_SRC}/video_core/*.h"
    "${CITRA_SRC}/audio_core/*.cpp"
    "${CITRA_SRC}/audio_core/*.h"
    "${CITRA_SRC}/input_common/*.cpp"
    "${CITRA_SRC}/input_common/*.h"
    "${CITRA_SRC}/common/*.cpp"
    "${CITRA_SRC}/common/*.h"
    "${CITRA_SRC}/network/*.cpp"
)

# ── JNI Bridge ────────────────────────────────────────────────
set(JNI_SOURCES
    android_jni.cpp
    emu_window_android.cpp
    input_android.cpp
    audio_android.cpp
)

# ── Main shared library ───────────────────────────────────────
add_library(citra-android SHARED
    ${JNI_SOURCES}
    ${CITRA_CORE_SOURCES}
)

# ── Include paths ─────────────────────────────────────────────
target_include_directories(citra-android PRIVATE
    ${CITRA_SRC}
    ${CITRA_SRC}/core
    ${CITRA_SRC}/common
    ${CMAKE_SOURCE_DIR}/externals/boost
    ${CMAKE_SOURCE_DIR}/externals/fmt/include
    ${CMAKE_SOURCE_DIR}/externals/cryptopp
    ${CMAKE_SOURCE_DIR}/externals/dynarmic/include
    ${CMAKE_SOURCE_DIR}/externals/nihstro/include
    ${CMAKE_SOURCE_DIR}/externals/teakra/include
    ${CMAKE_SOURCE_DIR}/externals/lodepng/lodepng
    ${CMAKE_SOURCE_DIR}/externals/soundtouch/include
)

# ── Compile definitions ───────────────────────────────────────
target_compile_definitions(citra-android PRIVATE
    ANDROID=1
    ENABLE_OPENGL=1
    $<$<BOOL:${ENABLE_VULKAN}>:ENABLE_VULKAN=1>
    $<$<BOOL:${ENABLE_JIT}>:ARCHITECTURE_arm64=1>
    CITRA_ARCH_ARM64=1
)

# ── Link libraries ────────────────────────────────────────────
find_library(LOG_LIB        log)
find_library(ANDROID_LIB    android)
find_library(EGL_LIB        EGL)
find_library(GLES3_LIB      GLESv3)
find_library(AAUDIO_LIB     aaudio)
find_library(OPENSLES_LIB   OpenSLES)

target_link_libraries(citra-android
    ${LOG_LIB}
    ${ANDROID_LIB}
    ${EGL_LIB}
    ${GLES3_LIB}
    ${AAUDIO_LIB}
    ${OPENSLES_LIB}
    z
)

# ── Vulkan ────────────────────────────────────────────────────
if(ENABLE_VULKAN)
    find_library(VULKAN_LIB vulkan)
    if(VULKAN_LIB)
        target_link_libraries(citra-android ${VULKAN_LIB})
        target_compile_definitions(citra-android PRIVATE USE_VULKAN=1)
    endif()
endif()

# ── Compiler flags ────────────────────────────────────────────
target_compile_options(citra-android PRIVATE
    -O3
    -ffast-math
    -fno-strict-aliasing
    -fvisibility=hidden
    -fPIC
    -Wno-error=deprecated-declarations
)

# ── Strip release build ───────────────────────────────────────
if(CMAKE_BUILD_TYPE STREQUAL "Release")
    add_custom_command(TARGET citra-android POST_BUILD
        COMMAND ${CMAKE_STRIP} --strip-unneeded
            $<TARGET_FILE:citra-android>
        COMMENT "Stripping binary..."
    )
endif()
