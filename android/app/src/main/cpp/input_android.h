// input_android.h
#pragma once

namespace InputCommon::Android {
    void  ButtonEvent  (int button, bool pressed);
    void  JoystickEvent(int stick, float x, float y);
    bool  IsButtonPressed(int button);
    float GetAxisX(int stick);
    float GetAxisY(int stick);
    void  Reset();
}
