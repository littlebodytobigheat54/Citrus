package com.citra.android.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.citra.android.CitraApplication
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("citra_settings")

object PreferenceManager {

    // ── Keys ───────────────────────────────────────────────────
    private val KEY_USE_VULKAN        = booleanPreferencesKey("use_vulkan")
    private val KEY_INTERNAL_RES      = intPreferencesKey("internal_res")
    private val KEY_CPU_CLOCK         = intPreferencesKey("cpu_clock")
    private val KEY_USE_SHADERS       = booleanPreferencesKey("use_shaders")
    private val KEY_AUDIO_VOLUME      = floatPreferencesKey("audio_volume")
    private val KEY_CONTROLS_OPACITY  = floatPreferencesKey("controls_opacity")
    private val KEY_CONTROLS_SCALE    = floatPreferencesKey("controls_scale")
    private val KEY_USE_HAPTIC        = booleanPreferencesKey("use_haptic")
    private val KEY_SHOW_PERF         = booleanPreferencesKey("show_perf")
    private val KEY_DARK_THEME        = booleanPreferencesKey("dark_theme")
    private val KEY_SCREEN_LAYOUT     = stringPreferencesKey("screen_layout")

    private val store get() = CitraApplication.context.dataStore

    fun init(context: Context) { /* dataStore is lazy, nothing to init */ }

    // ── Salvar todas as configurações ──────────────────────────
    suspend fun save(
        useVulkan:       Boolean = true,
        internalRes:     Int     = 2,
        cpuClock:        Int     = 100,
        useShaders:      Boolean = true,
        audioVolume:     Float   = 1f,
        controlsOpacity: Float   = 0.8f,
        controlsScale:   Float   = 1f,
        useHaptic:       Boolean = true,
        showPerfOverlay: Boolean = true,
        darkTheme:       Boolean = true
    ) {
        store.edit { prefs ->
            prefs[KEY_USE_VULKAN]       = useVulkan
            prefs[KEY_INTERNAL_RES]     = internalRes
            prefs[KEY_CPU_CLOCK]        = cpuClock
            prefs[KEY_USE_SHADERS]      = useShaders
            prefs[KEY_AUDIO_VOLUME]     = audioVolume
            prefs[KEY_CONTROLS_OPACITY] = controlsOpacity
            prefs[KEY_CONTROLS_SCALE]   = controlsScale
            prefs[KEY_USE_HAPTIC]       = useHaptic
            prefs[KEY_SHOW_PERF]        = showPerfOverlay
            prefs[KEY_DARK_THEME]       = darkTheme
        }
    }

    // ── Leituras individuais (Flow) ────────────────────────────
    val useVulkan:       Flow<Boolean> = store.data.map { it[KEY_USE_VULKAN]       ?: true  }
    val internalRes:     Flow<Int>     = store.data.map { it[KEY_INTERNAL_RES]     ?: 2     }
    val cpuClock:        Flow<Int>     = store.data.map { it[KEY_CPU_CLOCK]        ?: 100   }
    val useShaders:      Flow<Boolean> = store.data.map { it[KEY_USE_SHADERS]      ?: true  }
    val audioVolume:     Flow<Float>   = store.data.map { it[KEY_AUDIO_VOLUME]     ?: 1f    }
    val controlsOpacity: Flow<Float>   = store.data.map { it[KEY_CONTROLS_OPACITY] ?: 0.8f }
    val controlsScale:   Flow<Float>   = store.data.map { it[KEY_CONTROLS_SCALE]   ?: 1f   }
    val useHaptic:       Flow<Boolean> = store.data.map { it[KEY_USE_HAPTIC]       ?: true  }
    val showPerfOverlay: Flow<Boolean> = store.data.map { it[KEY_SHOW_PERF]        ?: true  }
    val darkTheme:       Flow<Boolean> = store.data.map { it[KEY_DARK_THEME]       ?: true  }

    // ── Leitura suspensa (para usar no JNI) ───────────────────
    suspend fun getSnapshot() = store.data.first().let { prefs ->
        SettingsSnapshot(
            useVulkan       = prefs[KEY_USE_VULKAN]       ?: true,
            internalRes     = prefs[KEY_INTERNAL_RES]     ?: 2,
            cpuClock        = prefs[KEY_CPU_CLOCK]        ?: 100,
            useShaders      = prefs[KEY_USE_SHADERS]      ?: true,
            audioVolume     = prefs[KEY_AUDIO_VOLUME]     ?: 1f,
            controlsOpacity = prefs[KEY_CONTROLS_OPACITY] ?: 0.8f,
            controlsScale   = prefs[KEY_CONTROLS_SCALE]   ?: 1f,
            useHaptic       = prefs[KEY_USE_HAPTIC]       ?: true,
            showPerfOverlay = prefs[KEY_SHOW_PERF]        ?: true,
            darkTheme       = prefs[KEY_DARK_THEME]       ?: true
        )
    }
}

data class SettingsSnapshot(
    val useVulkan:       Boolean,
    val internalRes:     Int,
    val cpuClock:        Int,
    val useShaders:      Boolean,
    val audioVolume:     Float,
    val controlsOpacity: Float,
    val controlsScale:   Float,
    val useHaptic:       Boolean,
    val showPerfOverlay: Boolean,
    val darkTheme:       Boolean
)
