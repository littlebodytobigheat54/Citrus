package com.citra.android.jni

import android.view.Surface

/**
 * Kotlin bridge para o núcleo C++ do Citra.
 * Todas as funções nativas são implementadas em android_jni.cpp
 */
object NativeLibrary {

    // ── Carregamento da biblioteca nativa ───────────────────────
    init {
        System.loadLibrary("citra-android")
    }

    // ── Ciclo de vida do emulador ───────────────────────────────

    /** Inicializa o core do Citra com configurações salvas */
    external fun initialize(): Boolean

    /** Carrega e inicia a emulação de um arquivo de jogo */
    external fun loadGame(path: String): Boolean

    /** Para a emulação e libera recursos */
    external fun stopEmulation()

    /** Pausa/retoma a emulação */
    external fun pauseEmulation()
    external fun resumeEmulation()

    /** Retorna true se a emulação está rodando */
    external fun isRunning(): Boolean

    // ── Surface / Renderer ─────────────────────────────────────

    /** Conecta a Surface Android ao renderer */
    external fun setSurface(surface: Surface, width: Int, height: Int)

    /** Notifica mudança de tamanho da surface */
    external fun surfaceChanged(surface: Surface, width: Int, height: Int)

    /** Destrói a surface */
    external fun surfaceDestroyed()

    // ── Input ──────────────────────────────────────────────────

    /**
     * Envia estado de botão.
     * @param button Código do botão (ver ButtonMap)
     * @param pressed true = pressionado, false = solto
     */
    external fun onGamePadButtonEvent(button: Int, pressed: Boolean)

    /**
     * Envia posição do analógico.
     * @param stick 0 = esquerdo (Circle Pad), 1 = direito (C-Stick)
     * @param x valor -1.0 a 1.0
     * @param y valor -1.0 a 1.0
     */
    external fun onGamePadJoystickEvent(stick: Int, x: Float, y: Float)

    /**
     * Envia toque na tela.
     * @param x posição relativa (0.0 a 1.0)
     * @param y posição relativa (0.0 a 1.0)
     */
    external fun onTouchEvent(x: Float, y: Float, pressed: Boolean)

    /** Move toque existente */
    external fun onTouchMoved(x: Float, y: Float)

    /** Termina toque */
    external fun onTouchReleased()

    // ── Save States ────────────────────────────────────────────

    /** Salva estado no slot indicado (0-9) */
    external fun saveState(slot: Int): Boolean

    /** Carrega estado do slot indicado */
    external fun loadState(slot: Int): Boolean

    /** Retorna true se existe save state no slot */
    external fun hasSaveState(slot: Int): Boolean

    // ── Configurações ──────────────────────────────────────────

    /** Aplica configurações de emulação (lidas do DataStore) */
    external fun applySettings(
        internalResolution: Int,   // 1=nativa, 2=2x, 3=3x, 4=4x
        useVulkan: Boolean,
        cpuClockPercentage: Int,   // 50-400
        useShaders: Boolean,
        audioVolume: Float         // 0.0-1.0
    )

    /** Retorna FPS atual */
    external fun getFPS(): Float

    /** Retorna uso de CPU (%) */
    external fun getCPUUsage(): Float

    /** Retorna uso de GPU (%) */
    external fun getGPUUsage(): Float

    // ── Cheat Codes ────────────────────────────────────────────

    external fun addCheat(name: String, code: String, enabled: Boolean)
    external fun removeCheat(index: Int)
    external fun toggleCheat(index: Int, enabled: Boolean)

    // ── Screenshot ─────────────────────────────────────────────

    /** Captura tela e salva em path. Retorna true em sucesso */
    external fun takeScreenshot(outputPath: String): Boolean

    // ── Mapeamento de botões ────────────────────────────────────
    object ButtonMap {
        const val BUTTON_A        = 0
        const val BUTTON_B        = 1
        const val BUTTON_X        = 2
        const val BUTTON_Y        = 3
        const val BUTTON_L        = 4
        const val BUTTON_R        = 5
        const val BUTTON_ZL       = 6
        const val BUTTON_ZR       = 7
        const val BUTTON_START    = 8
        const val BUTTON_SELECT   = 9
        const val BUTTON_HOME     = 10
        const val BUTTON_UP       = 11
        const val BUTTON_DOWN     = 12
        const val BUTTON_LEFT     = 13
        const val BUTTON_RIGHT    = 14
        const val BUTTON_DEBUG    = 15
        const val BUTTON_GPIO14   = 16
        const val STICK_CIRCLE    = 0  // Analógico principal
        const val STICK_CSTICK    = 1  // C-Stick (New 3DS)
    }
}
