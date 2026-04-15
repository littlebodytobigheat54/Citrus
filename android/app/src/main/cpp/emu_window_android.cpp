// emu_window_android.cpp
#include "emu_window_android.h"
#include <EGL/egl.h>
#include <EGL/eglext.h>
#include <GLES3/gl3.h>
#include <android/log.h>
#include <stdexcept>

#define TAG "EmuWindow"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

EmuWindow_Android* EmuWindow_Android::s_instance = nullptr;

// ── Singleton ────────────────────────────────────────────────
EmuWindow_Android* EmuWindow_Android::GetInstance() {
    return s_instance;
}

void EmuWindow_Android::CreateInstance(ANativeWindow* surface) {
    if (!s_instance) {
        s_instance = new EmuWindow_Android(surface);
    }
}

void EmuWindow_Android::DestroyInstance() {
    delete s_instance;
    s_instance = nullptr;
}

// ── Constructor / Destructor ─────────────────────────────────
EmuWindow_Android::EmuWindow_Android(ANativeWindow* surface)
    : m_native_window(surface)
{
    if (!InitEGL()) {
        LOGE("Failed to initialize EGL context");
        return;
    }
    m_is_open = true;
    LOGI("EmuWindow created: %dx%d",
         ANativeWindow_getWidth(surface),
         ANativeWindow_getHeight(surface));
}

EmuWindow_Android::~EmuWindow_Android() {
    DestroyEGL();
}

// ── EGL Initialization ────────────────────────────────────────
bool EmuWindow_Android::InitEGL() {
    EGLDisplay display = eglGetDisplay(EGL_DEFAULT_DISPLAY);
    if (display == EGL_NO_DISPLAY) {
        LOGE("eglGetDisplay failed");
        return false;
    }

    EGLint major, minor;
    if (!eglInitialize(display, &major, &minor)) {
        LOGE("eglInitialize failed");
        return false;
    }
    LOGI("EGL version: %d.%d", major, minor);

    const EGLint attribs[] = {
        EGL_RENDERABLE_TYPE, EGL_OPENGL_ES3_BIT,
        EGL_SURFACE_TYPE,    EGL_WINDOW_BIT,
        EGL_BLUE_SIZE,  8,
        EGL_GREEN_SIZE, 8,
        EGL_RED_SIZE,   8,
        EGL_DEPTH_SIZE, 24,
        EGL_NONE
    };

    EGLConfig config;
    EGLint    numConfigs;
    if (!eglChooseConfig(display, attribs, &config, 1, &numConfigs) || numConfigs == 0) {
        LOGE("eglChooseConfig failed");
        return false;
    }

    EGLSurface surface = eglCreateWindowSurface(
        display, config, m_native_window, nullptr);
    if (surface == EGL_NO_SURFACE) {
        LOGE("eglCreateWindowSurface failed: 0x%x", eglGetError());
        return false;
    }

    const EGLint ctxAttribs[] = {
        EGL_CONTEXT_CLIENT_VERSION, 3,
        EGL_NONE
    };
    EGLContext context = eglCreateContext(
        display, config, EGL_NO_CONTEXT, ctxAttribs);
    if (context == EGL_NO_CONTEXT) {
        LOGE("eglCreateContext failed: 0x%x", eglGetError());
        return false;
    }

    if (!eglMakeCurrent(display, surface, surface, context)) {
        LOGE("eglMakeCurrent failed: 0x%x", eglGetError());
        return false;
    }

    m_egl_display = display;
    m_egl_surface = surface;
    m_egl_context = context;

    eglQuerySurface(display, surface, EGL_WIDTH,  &m_width);
    eglQuerySurface(display, surface, EGL_HEIGHT, &m_height);

    NotifyFramebufferLayoutChanged(
        Frontend::EmuWindow::FramebufferLayout{
            static_cast<unsigned>(m_width),
            static_cast<unsigned>(m_height),
            true
        }
    );

    LOGI("EGL initialized: %dx%d", m_width, m_height);
    return true;
}

void EmuWindow_Android::DestroyEGL() {
    if (m_egl_display != EGL_NO_DISPLAY) {
        eglMakeCurrent(
            static_cast<EGLDisplay>(m_egl_display),
            EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT
        );
        if (m_egl_context != EGL_NO_CONTEXT)
            eglDestroyContext(
                static_cast<EGLDisplay>(m_egl_display),
                static_cast<EGLContext>(m_egl_context)
            );
        if (m_egl_surface != EGL_NO_SURFACE)
            eglDestroySurface(
                static_cast<EGLDisplay>(m_egl_display),
                static_cast<EGLSurface>(m_egl_surface)
            );
        eglTerminate(static_cast<EGLDisplay>(m_egl_display));
    }
    m_egl_display = EGL_NO_DISPLAY;
    m_egl_context = EGL_NO_CONTEXT;
    m_egl_surface = EGL_NO_SURFACE;
    m_is_open     = false;
}

// ── EmuWindow interface ───────────────────────────────────────
void EmuWindow_Android::SwapBuffers() {
    eglSwapBuffers(
        static_cast<EGLDisplay>(m_egl_display),
        static_cast<EGLSurface>(m_egl_surface)
    );
}

void EmuWindow_Android::PollEvents() {
    // Events handled via JNI callbacks — nothing to do here
}

void EmuWindow_Android::MakeCurrent() {
    eglMakeCurrent(
        static_cast<EGLDisplay>(m_egl_display),
        static_cast<EGLSurface>(m_egl_surface),
        static_cast<EGLSurface>(m_egl_surface),
        static_cast<EGLContext>(m_egl_context)
    );
}

void EmuWindow_Android::DoneCurrent() {
    eglMakeCurrent(
        static_cast<EGLDisplay>(m_egl_display),
        EGL_NO_SURFACE, EGL_NO_SURFACE, EGL_NO_CONTEXT
    );
}

// ── Surface change ────────────────────────────────────────────
void EmuWindow_Android::OnSurfaceChanged(ANativeWindow* surface, int w, int h) {
    m_native_window = surface;
    m_width  = w;
    m_height = h;
    LOGI("Surface changed: %dx%d", w, h);

    NotifyFramebufferLayoutChanged(
        Frontend::EmuWindow::FramebufferLayout{
            static_cast<unsigned>(w),
            static_cast<unsigned>(h),
            true
        }
    );
}

// ── Touch input ───────────────────────────────────────────────
void EmuWindow_Android::TouchPressed(unsigned x, unsigned y) {
    auto [adjusted_x, adjusted_y] = ClipToTouchScreen(x, y);
    TouchPressed(adjusted_x, adjusted_y);
    touch_state->touch_x = adjusted_x;
    touch_state->touch_y = adjusted_y;
    touch_state->touch_pressed = true;
}

void EmuWindow_Android::TouchMoved(unsigned x, unsigned y) {
    if (touch_state->touch_pressed) {
        auto [adjusted_x, adjusted_y] = ClipToTouchScreen(x, y);
        TouchMoved(adjusted_x, adjusted_y);
        touch_state->touch_x = adjusted_x;
        touch_state->touch_y = adjusted_y;
    }
}

void EmuWindow_Android::TouchReleased() {
    TouchReleased();
    touch_state->touch_pressed = false;
}

// ── Framebuffer size ──────────────────────────────────────────
std::pair<unsigned, unsigned> EmuWindow_Android::GetFramebufferSize() const {
    return { static_cast<unsigned>(m_width), static_cast<unsigned>(m_height) };
}
