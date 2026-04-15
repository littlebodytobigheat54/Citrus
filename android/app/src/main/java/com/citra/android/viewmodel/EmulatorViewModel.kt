package com.citra.android.viewmodel

import android.view.Surface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citra.android.jni.NativeLibrary
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

data class PerformanceStats(
    val fps: Float   = 0f,
    val cpu: Float   = 0f,
    val gpu: Float   = 0f
)

enum class EmulatorState {
    IDLE, LOADING, RUNNING, PAUSED, ERROR, STOPPED
}

class EmulatorViewModel : ViewModel() {

    private val _state = MutableStateFlow(EmulatorState.IDLE)
    val state: StateFlow<EmulatorState> = _state.asStateFlow()

    private val _perfStats = MutableStateFlow(PerformanceStats())
    val perfStats: StateFlow<PerformanceStats> = _perfStats.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _showControls = MutableStateFlow(true)
    val showControls: StateFlow<Boolean> = _showControls.asStateFlow()

    private var perfJob: Job? = null

    // ── Lifecycle ───────────────────────────────────────────────

    fun loadAndStart(gameUri: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = EmulatorState.LOADING

            val initialized = NativeLibrary.initialize()
            if (!initialized) {
                _state.value = EmulatorState.ERROR
                _errorMessage.value = "Falha ao inicializar o emulador"
                return@launch
            }

            val loaded = NativeLibrary.loadGame(gameUri)
            if (!loaded) {
                _state.value = EmulatorState.ERROR
                _errorMessage.value = "Não foi possível carregar o jogo"
                return@launch
            }

            _state.value = EmulatorState.RUNNING
            startPerfMonitor()
        }
    }

    fun pause() {
        if (_state.value == EmulatorState.RUNNING) {
            NativeLibrary.pauseEmulation()
            _state.value = EmulatorState.PAUSED
            perfJob?.cancel()
        }
    }

    fun resume() {
        if (_state.value == EmulatorState.PAUSED) {
            NativeLibrary.resumeEmulation()
            _state.value = EmulatorState.RUNNING
            startPerfMonitor()
        }
    }

    fun stop() {
        perfJob?.cancel()
        NativeLibrary.stopEmulation()
        _state.value = EmulatorState.STOPPED
    }

    // ── Surface ─────────────────────────────────────────────────

    fun onSurfaceCreated(surface: Surface, width: Int, height: Int) {
        NativeLibrary.setSurface(surface, width, height)
    }

    fun onSurfaceChanged(surface: Surface, width: Int, height: Int) {
        NativeLibrary.surfaceChanged(surface, width, height)
    }

    fun onSurfaceDestroyed() {
        NativeLibrary.surfaceDestroyed()
    }

    // ── Input ───────────────────────────────────────────────────

    fun onButtonDown(button: Int) =
        NativeLibrary.onGamePadButtonEvent(button, true)

    fun onButtonUp(button: Int) =
        NativeLibrary.onGamePadButtonEvent(button, false)

    fun onJoystickMoved(stick: Int, x: Float, y: Float) =
        NativeLibrary.onGamePadJoystickEvent(stick, x, y)

    fun onTouchDown(x: Float, y: Float) =
        NativeLibrary.onTouchEvent(x, y, true)

    fun onTouchMoved(x: Float, y: Float) =
        NativeLibrary.onTouchMoved(x, y)

    fun onTouchUp() =
        NativeLibrary.onTouchReleased()

    // ── Save States ─────────────────────────────────────────────

    fun saveState(slot: Int): Boolean = NativeLibrary.saveState(slot)
    fun loadState(slot: Int): Boolean = NativeLibrary.loadState(slot)
    fun hasSaveState(slot: Int): Boolean = NativeLibrary.hasSaveState(slot)

    // ── UI Controls ─────────────────────────────────────────────

    fun toggleControls() {
        _showControls.value = !_showControls.value
    }

    fun takeScreenshot(path: String): Boolean =
        NativeLibrary.takeScreenshot(path)

    // ── Internal ────────────────────────────────────────────────

    private fun startPerfMonitor() {
        perfJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                delay(500L)
                _perfStats.value = PerformanceStats(
                    fps = NativeLibrary.getFPS(),
                    cpu = NativeLibrary.getCPUUsage(),
                    gpu = NativeLibrary.getGPUUsage()
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stop()
    }
}
