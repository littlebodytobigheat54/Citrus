package com.citra.android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.citra.android.ui.EmulatorScreen
import com.citra.android.ui.theme.CitraTheme
import com.citra.android.viewmodel.EmulatorViewModel

class EmulatorActivity : ComponentActivity() {

    private val viewModel: EmulatorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupFullscreen()

        val gameUri = intent.getStringExtra(EXTRA_GAME_URI) ?: run {
            finish()
            return
        }

        setContent {
            CitraTheme(darkTheme = true) {
                EmulatorScreen(
                    gameUri    = gameUri,
                    viewModel  = viewModel,
                    onExit     = { finish() }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.resume()
    }

    override fun onPause() {
        super.onPause()
        viewModel.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stop()
    }

    private fun setupFullscreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.hide(WindowInsetsCompat.Type.systemBars())
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    companion object {
        const val EXTRA_GAME_URI = "game_uri"

        fun launch(context: Context, gameUri: String) {
            context.startActivity(
                Intent(context, EmulatorActivity::class.java).apply {
                    putExtra(EXTRA_GAME_URI, gameUri)
                }
            )
        }
    }
}
