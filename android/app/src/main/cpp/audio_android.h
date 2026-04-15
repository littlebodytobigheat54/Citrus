// audio_android.h
#pragma once
#include <cstdint>
#include <cstddef>

namespace AudioAndroid {
    bool Init();
    void Shutdown();
    void QueueSamples(const int16_t* data, size_t frameCount);
    void SetVolume(float volume); // 0.0 – 1.0
}
