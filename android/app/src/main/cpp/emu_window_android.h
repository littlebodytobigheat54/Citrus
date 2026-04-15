// emu_window_android.h — Implementação da janela de emulação para Android
#pragma once

#include <memory>
#include <android/native_window.h>
#include "citra-src/core/frontend/emu_window.h"

class EmuWindow_Android : public Frontend::EmuWindow {
public:
    explicit EmuWindow_Android(ANativeWindow* surface);
    ~EmuWindow_Android() override;

    // ── Singleton ────────────────────────────────────────────
    static EmuWindow_Android* GetInstance();
    static void                CreateInstance(ANativeWindow* surface);
    static void                DestroyInstance();

    // ── Surface callbacks ─────────────────────────────────────
    void OnSurfaceChanged(ANativeWindow* surface, int width, int height);

    // ── EmuWindow interface ───────────────────────────────────
    void SwapBuffers()  override;
    void PollEvents()   override;
    void MakeCurrent()  override;
    void DoneCurrent()  override;

    // ── Touch input ───────────────────────────────────────────
    void TouchPressed(unsigned framebuffer_x, unsigned framebuffer_y);
    void TouchMoved  (unsigned framebuffer_x, unsigned framebuffer_y);
    void TouchReleased();

    // ── Window size ───────────────────────────────────────────
    std::pair<unsigned, unsigned> GetFramebufferSize() const;
    bool IsOpen()  const { return m_is_open; }

private:
    static EmuWindow_Android* s_instance;

    ANativeWindow* m_native_window = nullptr;
    void*          m_egl_display   = nullptr;
    void*          m_egl_context   = nullptr;
    void*          m_egl_surface   = nullptr;

    int  m_width    = 0;
    int  m_height   = 0;
    bool m_is_open  = false;

    bool InitEGL();
    void DestroyEGL();
};
