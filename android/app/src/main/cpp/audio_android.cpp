// audio_android.cpp — Saída de áudio via AAudio
#include "audio_android.h"
#include <aaudio/AAudio.h>
#include <android/log.h>
#include <vector>
#include <mutex>
#include <atomic>

#define TAG "AudioAndroid"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)

static AAudioStream*          s_stream     = nullptr;
static std::vector<int16_t>   s_audio_buf;
static std::mutex             s_buf_mutex;
static std::atomic<float>     s_volume{1.f};

static constexpr int SAMPLE_RATE    = 32728; // 3DS native
static constexpr int CHANNEL_COUNT  = 2;
static constexpr int FRAMES_PER_CB  = 256;

// ── AAudio data callback ───────────────────────────────────────
static aaudio_data_callback_result_t AudioCallback(
    AAudioStream*   /*stream*/,
    void*           /*userData*/,
    void*           audioData,
    int32_t         numFrames)
{
    auto*  out    = static_cast<int16_t*>(audioData);
    size_t needed = static_cast<size_t>(numFrames * CHANNEL_COUNT);

    std::lock_guard<std::mutex> lock(s_buf_mutex);
    const float vol = s_volume.load(std::memory_order_relaxed);

    if (s_audio_buf.size() >= needed) {
        for (size_t i = 0; i < needed; ++i) {
            out[i] = static_cast<int16_t>(s_audio_buf[i] * vol);
        }
        s_audio_buf.erase(s_audio_buf.begin(),
                          s_audio_buf.begin() + needed);
    } else {
        // Buffer underrun — fill silence
        std::fill(out, out + needed, int16_t{0});
    }
    return AAUDIO_CALLBACK_RESULT_CONTINUE;
}

namespace AudioAndroid {

bool Init() {
    AAudioStreamBuilder* builder;
    if (AAudio_createStreamBuilder(&builder) != AAUDIO_OK) {
        LOGE("Failed to create AAudio stream builder");
        return false;
    }

    AAudioStreamBuilder_setFormat         (builder, AAUDIO_FORMAT_PCM_I16);
    AAudioStreamBuilder_setSampleRate     (builder, SAMPLE_RATE);
    AAudioStreamBuilder_setChannelCount   (builder, CHANNEL_COUNT);
    AAudioStreamBuilder_setPerformanceMode(builder, AAUDIO_PERFORMANCE_MODE_LOW_LATENCY);
    AAudioStreamBuilder_setDataCallback   (builder, AudioCallback, nullptr);
    AAudioStreamBuilder_setFramesPerDataCallback(builder, FRAMES_PER_CB);

    aaudio_result_t result = AAudioStreamBuilder_openStream(builder, &s_stream);
    AAudioStreamBuilder_delete(builder);

    if (result != AAUDIO_OK) {
        LOGE("Failed to open AAudio stream: %s", AAudio_convertResultToText(result));
        return false;
    }

    if (AAudioStream_requestStart(s_stream) != AAUDIO_OK) {
        LOGE("Failed to start AAudio stream");
        return false;
    }

    LOGI("AAudio initialized: %dHz, %dch", SAMPLE_RATE, CHANNEL_COUNT);
    return true;
}

void Shutdown() {
    if (s_stream) {
        AAudioStream_requestStop(s_stream);
        AAudioStream_close(s_stream);
        s_stream = nullptr;
        LOGI("AAudio shutdown");
    }
}

void QueueSamples(const int16_t* data, size_t frameCount) {
    std::lock_guard<std::mutex> lock(s_buf_mutex);
    const size_t sampleCount = frameCount * CHANNEL_COUNT;
    // Cap buffer at 2 seconds to avoid unbounded growth
    const size_t maxSamples = SAMPLE_RATE * CHANNEL_COUNT * 2;
    if (s_audio_buf.size() + sampleCount > maxSamples) return;
    s_audio_buf.insert(s_audio_buf.end(), data, data + sampleCount);
}

void SetVolume(float volume) {
    s_volume.store(
        std::max(0.f, std::min(1.f, volume)),
        std::memory_order_relaxed
    );
}

} // namespace AudioAndroid
