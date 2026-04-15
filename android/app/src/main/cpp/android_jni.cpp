// android_jni.cpp — Bridge entre Kotlin/Java e o núcleo C++ do Citra
#include <jni.h>
#include <android/native_window_jni.h>
#include <android/log.h>
#include <string>
#include <memory>
#include <atomic>

// ── Includes do Citra Core ─────────────────────────────────────
// (esses paths assumem que o source do Lime3DS/Citra está em citra-src/)
#include "citra-src/core/core.h"
#include "citra-src/core/settings.h"
#include "citra-src/core/frontend/emu_window.h"
#include "citra-src/video_core/renderer_base.h"
#include "citra-src/input_common/main.h"
#include "citra-src/audio_core/dsp_interface.h"

#define TAG "CitraJNI"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)

// ── Estado Global ──────────────────────────────────────────────
static std::atomic<bool> s_is_running{false};
static std::atomic<bool> s_is_paused{false};
static ANativeWindow*    s_native_window = nullptr;
static Core::System&     s_system = Core::System::GetInstance();

// ── Helpers ────────────────────────────────────────────────────
static std::string JStringToStd(JNIEnv* env, jstring jstr) {
    if (!jstr) return {};
    const char* chars = env->GetStringUTFChars(jstr, nullptr);
    std::string result(chars);
    env->ReleaseStringUTFChars(jstr, chars);
    return result;
}

extern "C" {

// ── initialize() ──────────────────────────────────────────────
JNIEXPORT jboolean JNICALL
Java_com_citra_android_jni_NativeLibrary_initialize(JNIEnv* env, jobject) {
    LOGI("Initializing Citra core...");

    InputCommon::Init();

    Settings::values.use_cpu_jit.SetValue(true);
    Settings::values.cpu_clock_percentage.SetValue(100);
    Settings::values.use_hw_renderer.SetValue(true);
    Settings::values.use_hw_shader.SetValue(true);
    Settings::values.use_shader_jit.SetValue(true);
    Settings::values.use_vsync_new.SetValue(true);

    LOGI("Core initialized successfully");
    return JNI_TRUE;
}

// ── loadGame() ────────────────────────────────────────────────
JNIEXPORT jboolean JNICALL
Java_com_citra_android_jni_NativeLibrary_loadGame(JNIEnv* env, jobject, jstring jpath) {
    std::string path = JStringToStd(env, jpath);
    LOGI("Loading game: %s", path.c_str());

    Core::System::ResultStatus result = s_system.Load(
        *EmuWindow_Android::GetInstance(), path
    );

    if (result != Core::System::ResultStatus::Success) {
        LOGE("Failed to load game, error code: %d", static_cast<int>(result));
        return JNI_FALSE;
    }

    s_is_running.store(true);
    s_is_paused.store(false);
    LOGI("Game loaded successfully");
    return JNI_TRUE;
}

// ── stopEmulation() ───────────────────────────────────────────
JNIEXPORT void JNICALL
Java_com_citra_android_jni_NativeLibrary_stopEmulation(JNIEnv*, jobject) {
    LOGI("Stopping emulation...");
    s_is_running.store(false);
    s_system.Shutdown();
    InputCommon::Shutdown();
    LOGI("Emulation stopped");
}

// ── pauseEmulation() ──────────────────────────────────────────
JNIEXPORT void JNICALL
Java_com_citra_android_jni_NativeLibrary_pauseEmulation(JNIEnv*, jobject) {
    if (s_is_running && !s_is_paused) {
        s_system.SetRunning(false);
        s_is_paused.store(true);
        LOGI("Emulation paused");
    }
}

// ── resumeEmulation() ─────────────────────────────────────────
JNIEXPORT void JNICALL
Java_com_citra_android_jni_NativeLibrary_resumeEmulation(JNIEnv*, jobject) {
    if (s_is_running && s_is_paused) {
        s_system.SetRunning(true);
        s_is_paused.store(false);
        LOGI("Emulation resumed");
    }
}

// ── isRunning() ───────────────────────────────────────────────
JNIEXPORT jboolean JNICALL
Java_com_citra_android_jni_NativeLibrary_isRunning(JNIEnv*, jobject) {
    return static_cast<jboolean>(s_is_running.load());
}

// ── setSurface() ──────────────────────────────────────────────
JNIEXPORT void JNICALL
Java_com_citra_android_jni_NativeLibrary_setSurface(
    JNIEnv* env, jobject, jobject surface, jint width, jint height)
{
    if (s_native_window) {
        ANativeWindow_release(s_native_window);
    }
    s_native_window = ANativeWindow_fromSurface(env, surface);
    LOGI("Surface set: %dx%d", width, height);

    EmuWindow_Android* emu_window = EmuWindow_Android::GetInstance();
    if (emu_window) {
        emu_window->OnSurfaceChanged(s_native_window, width, height);
    }
}

// ── surfaceChanged() ──────────────────────────────────────────
JNIEXPORT void JNICALL
Java_com_citra_android_jni_NativeLibrary_surfaceChanged(
    JNIEnv* env, jobject, jobject surface, jint width, jint height)
{
    s_native_window = ANativeWindow_fromSurface(env, surface);
    EmuWindow_Android* emu_window = EmuWindow_Android::GetInstance();
    if (emu_window) {
        emu_window->OnSurfaceChanged(s_native_window, width, height);
    }
}

// ── surfaceDestroyed() ────────────────────────────────────────
JNIEXPORT void JNICALL
Java_com_citra_android_jni_NativeLibrary_surfaceDestroyed(JNIEnv*, jobject) {
    if (s_native_window) {
        ANativeWindow_release(s_native_window);
        s_native_window = nullptr;
    }
}

// ── onGamePadButtonEvent() ────────────────────────────────────
JNIEXPORT void JNICALL
Java_com_citra_android_jni_NativeLibrary_onGamePadButtonEvent(
    JNIEnv*, jobject, jint button, jboolean pressed)
{
    InputCommon::Android::ButtonEvent(button, pressed == JNI_TRUE);
}

// ── onGamePadJoystickEvent() ──────────────────────────────────
JNIEXPORT void JNICALL
Java_com_citra_android_jni_NativeLibrary_onGamePadJoystickEvent(
    JNIEnv*, jobject, jint stick, jfloat x, jfloat y)
{
    InputCommon::Android::JoystickEvent(stick, x, y);
}

// ── onTouchEvent() ────────────────────────────────────────────
JNIEXPORT void JNICALL
Java_com_citra_android_jni_NativeLibrary_onTouchEvent(
    JNIEnv*, jobject, jfloat x, jfloat y, jboolean pressed)
{
    EmuWindow_Android* emu_window = EmuWindow_Android::GetInstance();
    if (!emu_window) return;

    if (pressed) {
        emu_window->TouchPressed(static_cast<unsigned>(x), static_cast<unsigned>(y));
    } else {
        emu_window->TouchReleased();
    }
}

// ── onTouchMoved() ────────────────────────────────────────────
JNIEXPORT void JNICALL
Java_com_citra_android_jni_NativeLibrary_onTouchMoved(
    JNIEnv*, jobject, jfloat x, jfloat y)
{
    EmuWindow_Android* emu_window = EmuWindow_Android::GetInstance();
    if (emu_window) {
        emu_window->TouchMoved(static_cast<unsigned>(x), static_cast<unsigned>(y));
    }
}

// ── onTouchReleased() ─────────────────────────────────────────
JNIEXPORT void JNICALL
Java_com_citra_android_jni_NativeLibrary_onTouchReleased(JNIEnv*, jobject) {
    EmuWindow_Android* emu_window = EmuWindow_Android::GetInstance();
    if (emu_window) {
        emu_window->TouchReleased();
    }
}

// ── saveState() ───────────────────────────────────────────────
JNIEXPORT jboolean JNICALL
Java_com_citra_android_jni_NativeLibrary_saveState(JNIEnv*, jobject, jint slot) {
    if (!s_is_running) return JNI_FALSE;
    try {
        Core::SaveState(static_cast<u32>(slot));
        LOGI("State saved to slot %d", slot);
        return JNI_TRUE;
    } catch (const std::exception& e) {
        LOGE("Save state failed: %s", e.what());
        return JNI_FALSE;
    }
}

// ── loadState() ───────────────────────────────────────────────
JNIEXPORT jboolean JNICALL
Java_com_citra_android_jni_NativeLibrary_loadState(JNIEnv*, jobject, jint slot) {
    if (!s_is_running) return JNI_FALSE;
    try {
        Core::LoadState(static_cast<u32>(slot));
        LOGI("State loaded from slot %d", slot);
        return JNI_TRUE;
    } catch (const std::exception& e) {
        LOGE("Load state failed: %s", e.what());
        return JNI_FALSE;
    }
}

// ── hasSaveState() ────────────────────────────────────────────
JNIEXPORT jboolean JNICALL
Java_com_citra_android_jni_NativeLibrary_hasSaveState(JNIEnv*, jobject, jint slot) {
    return static_cast<jboolean>(Core::HasSaveState(static_cast<u32>(slot)));
}

// ── applySettings() ───────────────────────────────────────────
JNIEXPORT void JNICALL
Java_com_citra_android_jni_NativeLibrary_applySettings(
    JNIEnv*, jobject,
    jint  internalResolution,
    jboolean useVulkan,
    jint  cpuClockPercentage,
    jboolean useShaders,
    jfloat audioVolume)
{
    Settings::values.resolution_factor.SetValue(
        static_cast<u16>(internalResolution));
    Settings::values.use_vulkan_renderer.SetValue(useVulkan == JNI_TRUE);
    Settings::values.cpu_clock_percentage.SetValue(
        static_cast<u32>(cpuClockPercentage));
    Settings::values.use_hw_shader.SetValue(useShaders == JNI_TRUE);
    Settings::values.volume.SetValue(audioVolume);

    if (s_is_running) {
        s_system.ApplySettings();
    }
    LOGI("Settings applied: res=%d vulkan=%d cpu=%d%%",
         internalResolution, useVulkan, cpuClockPercentage);
}

// ── Performance Metrics ───────────────────────────────────────
JNIEXPORT jfloat JNICALL
Java_com_citra_android_jni_NativeLibrary_getFPS(JNIEnv*, jobject) {
    if (!s_is_running) return 0.0f;
    return static_cast<jfloat>(s_system.GetIPCRecorder().GetFrameRate());
}

JNIEXPORT jfloat JNICALL
Java_com_citra_android_jni_NativeLibrary_getCPUUsage(JNIEnv*, jobject) {
    if (!s_is_running) return 0.0f;
    return static_cast<jfloat>(s_system.PerfStats().GetCPUUsagePercent());
}

JNIEXPORT jfloat JNICALL
Java_com_citra_android_jni_NativeLibrary_getGPUUsage(JNIEnv*, jobject) {
    if (!s_is_running) return 0.0f;
    return static_cast<jfloat>(s_system.PerfStats().GetGPUUsagePercent());
}

// ── takeScreenshot() ──────────────────────────────────────────
JNIEXPORT jboolean JNICALL
Java_com_citra_android_jni_NativeLibrary_takeScreenshot(
    JNIEnv* env, jobject, jstring jpath)
{
    std::string path = JStringToStd(env, jpath);
    if (!s_is_running) return JNI_FALSE;
    try {
        s_system.GetRenderer().RequestScreenshot(path);
        return JNI_TRUE;
    } catch (...) {
        return JNI_FALSE;
    }
}

} // extern "C"
