// input_android.cpp — Mapeamento de input Android → Citra
#include "input_android.h"
#include <android/log.h>
#include <array>
#include <atomic>
#include <cmath>

#define TAG "InputAndroid"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  TAG, __VA_ARGS__)

// ── Estado dos botões (thread-safe) ───────────────────────────
static std::array<std::atomic<bool>, 32> s_button_states{};

// ── Estado dos analógicos ─────────────────────────────────────
struct StickState {
    std::atomic<float> x{0.f};
    std::atomic<float> y{0.f};
};
static std::array<StickState, 2> s_stick_states{};

namespace InputCommon::Android {

void ButtonEvent(int button, bool pressed) {
    if (button < 0 || button >= static_cast<int>(s_button_states.size())) return;
    s_button_states[button].store(pressed, std::memory_order_relaxed);
}

void JoystickEvent(int stick, float x, float y) {
    if (stick < 0 || stick >= static_cast<int>(s_stick_states.size())) return;

    // Dead zone de 10%
    const float dead_zone = 0.10f;
    if (std::abs(x) < dead_zone) x = 0.f;
    if (std::abs(y) < dead_zone) y = 0.f;

    // Clamp -1..1
    x = std::max(-1.f, std::min(1.f, x));
    y = std::max(-1.f, std::min(1.f, y));

    s_stick_states[stick].x.store(x, std::memory_order_relaxed);
    s_stick_states[stick].y.store(y, std::memory_order_relaxed);
}

bool IsButtonPressed(int button) {
    if (button < 0 || button >= static_cast<int>(s_button_states.size())) return false;
    return s_button_states[button].load(std::memory_order_relaxed);
}

float GetAxisX(int stick) {
    if (stick < 0 || stick >= static_cast<int>(s_stick_states.size())) return 0.f;
    return s_stick_states[stick].x.load(std::memory_order_relaxed);
}

float GetAxisY(int stick) {
    if (stick < 0 || stick >= static_cast<int>(s_stick_states.size())) return 0.f;
    return s_stick_states[stick].y.load(std::memory_order_relaxed);
}

void Reset() {
    for (auto& btn : s_button_states) btn.store(false, std::memory_order_relaxed);
    for (auto& stick : s_stick_states) {
        stick.x.store(0.f, std::memory_order_relaxed);
        stick.y.store(0.f, std::memory_order_relaxed);
    }
}

} // namespace InputCommon::Android
