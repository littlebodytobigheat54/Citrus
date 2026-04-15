package com.citra.android.ui

import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.citra.android.viewmodel.EmulatorState
import com.citra.android.viewmodel.EmulatorViewModel
import kotlinx.coroutines.delay

// ── Modos de layout ────────────────────────────────────────────
enum class ScreenLayout {
    LANDSCAPE_SIDE_BY_SIDE,   // telas lado a lado (horizontal)
    PORTRAIT_STACKED,          // telas empilhadas (vertical)
    SINGLE_TOP,                // só tela de cima
    SINGLE_BOTTOM,             // só tela de baixo
    SWAPPED                    // telas trocadas
}

@Composable
fun EmulatorScreen(
    gameUri:   String,
    viewModel: EmulatorViewModel,
    onExit:    () -> Unit
) {
    val state      by viewModel.state.collectAsState()
    val perfStats  by viewModel.perfStats.collectAsState()
    val showCtrls  by viewModel.showControls.collectAsState()

    var showMenu      by remember { mutableStateOf(false) }
    var screenLayout  by remember { mutableStateOf(ScreenLayout.LANDSCAPE_SIDE_BY_SIDE) }
    var showPerfOverlay by remember { mutableStateOf(true) }
    var showSaveDialog  by remember { mutableStateOf(false) }

    // Inicia emulação quando a screen é composta
    LaunchedEffect(gameUri) {
        viewModel.loadAndStart(gameUri)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // ── Telas do 3DS ──────────────────────────────────────
        when (screenLayout) {
            ScreenLayout.LANDSCAPE_SIDE_BY_SIDE -> {
                Row(Modifier.fillMaxSize()) {
                    TopScreen(viewModel, Modifier.weight(1.6f).fillMaxHeight())
                    BottomScreen(viewModel, Modifier.weight(1f).fillMaxHeight())
                }
            }
            ScreenLayout.PORTRAIT_STACKED -> {
                Column(Modifier.fillMaxSize()) {
                    TopScreen(viewModel, Modifier.weight(1f).fillMaxWidth())
                    BottomScreen(viewModel, Modifier.weight(0.75f).fillMaxWidth())
                }
            }
            ScreenLayout.SINGLE_TOP -> {
                TopScreen(viewModel, Modifier.fillMaxSize())
            }
            ScreenLayout.SINGLE_BOTTOM -> {
                BottomScreen(viewModel, Modifier.fillMaxSize())
            }
            ScreenLayout.SWAPPED -> {
                Row(Modifier.fillMaxSize()) {
                    BottomScreen(viewModel, Modifier.weight(1f).fillMaxHeight())
                    TopScreen(viewModel, Modifier.weight(1.6f).fillMaxHeight())
                }
            }
        }

        // ── Overlay de controles virtuais ─────────────────────
        AnimatedVisibility(
            visible = showCtrls,
            enter   = fadeIn(),
            exit    = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            VirtualControlsOverlay(
                viewModel = viewModel,
                modifier  = Modifier.fillMaxSize()
            )
        }

        // ── Overlay de performance ────────────────────────────
        AnimatedVisibility(
            visible = showPerfOverlay,
            modifier = Modifier.align(Alignment.TopStart).padding(8.dp)
        ) {
            PerformanceOverlay(
                fps = perfStats.fps,
                cpu = perfStats.cpu,
                gpu = perfStats.gpu
            )
        }

        // ── Menu flutuante ────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp)
        ) {
            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, "Menu",
                    tint = Color.White.copy(alpha = 0.8f))
            }

            DropdownMenu(
                expanded         = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                // Layout selector
                Text("Layout", modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)

                ScreenLayout.values().forEach { layout ->
                    DropdownMenuItem(
                        text = { Text(layout.displayName()) },
                        onClick = { screenLayout = layout; showMenu = false },
                        leadingIcon = {
                            if (screenLayout == layout)
                                Icon(Icons.Default.Check, null)
                        }
                    )
                }

                Divider()

                DropdownMenuItem(
                    text    = { Text("Save State") },
                    onClick = { showSaveDialog = true; showMenu = false },
                    leadingIcon = { Icon(Icons.Default.Save, null) }
                )
                DropdownMenuItem(
                    text    = { Text("Captura de Tela") },
                    onClick = { /* screenshot */ showMenu = false },
                    leadingIcon = { Icon(Icons.Default.Screenshot, null) }
                )
                DropdownMenuItem(
                    text    = { Text(if (showPerfOverlay) "Ocultar FPS" else "Mostrar FPS") },
                    onClick = { showPerfOverlay = !showPerfOverlay; showMenu = false },
                    leadingIcon = { Icon(Icons.Default.Speed, null) }
                )
                DropdownMenuItem(
                    text    = { Text(if (showCtrls) "Ocultar Controles" else "Mostrar Controles") },
                    onClick = { viewModel.toggleControls(); showMenu = false },
                    leadingIcon = { Icon(Icons.Default.Gamepad, null) }
                )

                Divider()

                DropdownMenuItem(
                    text    = { Text("Sair") },
                    onClick = { onExit() },
                    leadingIcon = { Icon(Icons.Default.ExitToApp, null) }
                )
            }
        }

        // ── Loading indicator ─────────────────────────────────
        if (state == EmulatorState.LOADING) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(Modifier.height(16.dp))
                    Text("Carregando jogo...", color = Color.White)
                }
            }
        }

        // ── Error state ───────────────────────────────────────
        if (state == EmulatorState.ERROR) {
            Box(
                Modifier.fillMaxSize().background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Error, null,
                        tint = Color.Red, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Erro ao carregar o jogo", color = Color.White,
                        style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = onExit) { Text("Voltar") }
                }
            }
        }

        // ── Save State Dialog ─────────────────────────────────
        if (showSaveDialog) {
            SaveStateDialog(
                viewModel = viewModel,
                onDismiss = { showSaveDialog = false }
            )
        }
    }
}

@Composable
fun TopScreen(viewModel: EmulatorViewModel, modifier: Modifier) {
    AndroidView(
        factory = { ctx ->
            SurfaceView(ctx).apply {
                holder.addCallback(object : SurfaceHolder.Callback {
                    override fun surfaceCreated(holder: SurfaceHolder) {
                        holder.surface?.let {
                            viewModel.onSurfaceCreated(it, width, height)
                        }
                    }
                    override fun surfaceChanged(holder: SurfaceHolder, f: Int, w: Int, h: Int) {
                        holder.surface?.let {
                            viewModel.onSurfaceChanged(it, w, h)
                        }
                    }
                    override fun surfaceDestroyed(holder: SurfaceHolder) {
                        viewModel.onSurfaceDestroyed()
                    }
                })
            }
        },
        modifier = modifier
    )
}

@Composable
fun BottomScreen(viewModel: EmulatorViewModel, modifier: Modifier) {
    AndroidView(
        factory = { ctx ->
            SurfaceView(ctx).apply {
                setOnTouchListener { _, event ->
                    val relX = event.x / width
                    val relY = event.y / height
                    when (event.action) {
                        android.view.MotionEvent.ACTION_DOWN ->
                            viewModel.onTouchDown(relX * 320f, relY * 240f)
                        android.view.MotionEvent.ACTION_MOVE ->
                            viewModel.onTouchMoved(relX * 320f, relY * 240f)
                        android.view.MotionEvent.ACTION_UP ->
                            viewModel.onTouchUp()
                    }
                    true
                }
            }
        },
        modifier = modifier
    )
}

@Composable
fun PerformanceOverlay(fps: Float, cpu: Float, gpu: Float) {
    Surface(
        color  = Color.Black.copy(alpha = 0.6f),
        shape  = MaterialTheme.shapes.small
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text("FPS: ${fps.toInt()}",   color = Color.Green,  fontSize = 11.sp)
            Text("CPU: ${cpu.toInt()}%",  color = Color.Yellow, fontSize = 11.sp)
            Text("GPU: ${gpu.toInt()}%",  color = Color.Cyan,   fontSize = 11.sp)
        }
    }
}

@Composable
fun SaveStateDialog(viewModel: EmulatorViewModel, onDismiss: () -> Unit) {
    var mode by remember { mutableStateOf("save") } // "save" or "load"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save States") },
        text = {
            Column {
                Row {
                    FilterChip(selected = mode == "save",
                        onClick = { mode = "save" }, label = { Text("Salvar") })
                    Spacer(Modifier.width(8.dp))
                    FilterChip(selected = mode == "load",
                        onClick = { mode = "load" }, label = { Text("Carregar") })
                }
                Spacer(Modifier.height(12.dp))
                // Slots 0-4
                (0..4).forEach { slot ->
                    val exists = viewModel.hasSaveState(slot)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Text("Slot ${slot + 1}", Modifier.weight(1f))
                        if (exists) {
                            Icon(Icons.Default.CheckCircle, null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp).padding(end = 8.dp))
                        }
                        Button(
                            onClick = {
                                if (mode == "save") viewModel.saveState(slot)
                                else viewModel.loadState(slot)
                                onDismiss()
                            },
                            enabled = mode == "save" || exists
                        ) {
                            Text(if (mode == "save") "Salvar" else "Carregar")
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Fechar") }
        }
    )
}

private fun ScreenLayout.displayName() = when (this) {
    ScreenLayout.LANDSCAPE_SIDE_BY_SIDE -> "Horizontal (lado a lado)"
    ScreenLayout.PORTRAIT_STACKED       -> "Vertical (empilhado)"
    ScreenLayout.SINGLE_TOP             -> "Tela superior"
    ScreenLayout.SINGLE_BOTTOM          -> "Tela inferior"
    ScreenLayout.SWAPPED                -> "Telas trocadas"
}
