package com.citra.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.citra.android.jni.NativeLibrary.ButtonMap
import com.citra.android.viewmodel.EmulatorViewModel
import kotlin.math.*

// ── Paleta de cores dos botões ─────────────────────────────────
private object ButtonColors {
    val A      = Color(0xFFE53935)   // Vermelho
    val B      = Color(0xFFFDD835)   // Amarelo
    val X      = Color(0xFF1E88E5)   // Azul
    val Y      = Color(0xFF43A047)   // Verde
    val L_R    = Color(0xFF546E7A)   // Cinza azulado
    val START  = Color(0xFF37474F)
    val SELECT = Color(0xFF37474F)
    val HOME   = Color(0xFF1565C0)
    val DPAD   = Color(0xFF263238)
    val STICK  = Color(0xFF455A64)
}

@Composable
fun VirtualControlsOverlay(
    viewModel: EmulatorViewModel,
    modifier:  Modifier = Modifier
) {
    Box(modifier = modifier) {

        // ── D-Pad (esquerda) ──────────────────────────────────
        DPad(
            onButtonDown = { viewModel.onButtonDown(it) },
            onButtonUp   = { viewModel.onButtonUp(it) },
            modifier     = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 24.dp)
                .size(140.dp)
        )

        // ── Analógico (Circle Pad) ────────────────────────────
        CirclePad(
            onMove = { x, y ->
                viewModel.onJoystickMoved(ButtonMap.STICK_CIRCLE, x, y)
            },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 170.dp)
                .size(110.dp)
        )

        // ── Botões ABXY (direita) ─────────────────────────────
        AbxyButtons(
            onButtonDown = { viewModel.onButtonDown(it) },
            onButtonUp   = { viewModel.onButtonUp(it) },
            modifier     = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 24.dp)
                .size(140.dp)
        )

        // ── Botões L / R ──────────────────────────────────────
        GameButton(
            label    = "L",
            color    = ButtonColors.L_R,
            onDown   = { viewModel.onButtonDown(ButtonMap.BUTTON_L) },
            onUp     = { viewModel.onButtonUp(ButtonMap.BUTTON_L) },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 12.dp, top = 12.dp)
                .size(width = 72.dp, height = 36.dp)
        )
        GameButton(
            label    = "R",
            color    = ButtonColors.L_R,
            onDown   = { viewModel.onButtonDown(ButtonMap.BUTTON_R) },
            onUp     = { viewModel.onButtonUp(ButtonMap.BUTTON_R) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 12.dp, top = 12.dp)
                .size(width = 72.dp, height = 36.dp)
        )

        // ── ZL / ZR (New 3DS) ─────────────────────────────────
        GameButton(
            label    = "ZL",
            color    = ButtonColors.L_R,
            onDown   = { viewModel.onButtonDown(ButtonMap.BUTTON_ZL) },
            onUp     = { viewModel.onButtonUp(ButtonMap.BUTTON_ZL) },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 96.dp, top = 12.dp)
                .size(width = 60.dp, height = 36.dp)
        )
        GameButton(
            label    = "ZR",
            color    = ButtonColors.L_R,
            onDown   = { viewModel.onButtonDown(ButtonMap.BUTTON_ZR) },
            onUp     = { viewModel.onButtonUp(ButtonMap.BUTTON_ZR) },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = 96.dp, top = 12.dp)
                .size(width = 60.dp, height = 36.dp)
        )

        // ── START / SELECT / HOME ─────────────────────────────
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SmallButton("SEL",
                onDown = { viewModel.onButtonDown(ButtonMap.BUTTON_SELECT) },
                onUp   = { viewModel.onButtonUp(ButtonMap.BUTTON_SELECT) })
            HomeButton(
                onDown = { viewModel.onButtonDown(ButtonMap.BUTTON_HOME) },
                onUp   = { viewModel.onButtonUp(ButtonMap.BUTTON_HOME) })
            SmallButton("STA",
                onDown = { viewModel.onButtonDown(ButtonMap.BUTTON_START) },
                onUp   = { viewModel.onButtonUp(ButtonMap.BUTTON_START) })
        }
    }
}

// ── D-Pad ──────────────────────────────────────────────────────
@Composable
fun DPad(
    onButtonDown: (Int) -> Unit,
    onButtonUp:   (Int) -> Unit,
    modifier:     Modifier
) {
    val buttons = listOf(
        Triple(ButtonMap.BUTTON_UP,    Alignment.TopCenter,    "▲"),
        Triple(ButtonMap.BUTTON_DOWN,  Alignment.BottomCenter, "▼"),
        Triple(ButtonMap.BUTTON_LEFT,  Alignment.CenterStart,  "◄"),
        Triple(ButtonMap.BUTTON_RIGHT, Alignment.CenterEnd,    "►"),
    )
    Box(modifier) {
        buttons.forEach { (btn, align, label) ->
            Box(
                modifier = Modifier
                    .align(align)
                    .size(44.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(ButtonColors.DPAD)
                    .pointerInput(btn) {
                        detectTapGestures(
                            onPress = {
                                onButtonDown(btn)
                                tryAwaitRelease()
                                onButtonUp(btn)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
        // Centro do d-pad
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(44.dp)
                .background(ButtonColors.DPAD)
        )
    }
}

// ── Circle Pad (analógico) ─────────────────────────────────────
@Composable
fun CirclePad(onMove: (Float, Float) -> Unit, modifier: Modifier) {
    var stickOffset by remember { mutableStateOf(Offset.Zero) }
    val maxRadius = 35f

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(ButtonColors.STICK.copy(alpha = 0.5f))
            .border(2.dp, ButtonColors.STICK, CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd   = { stickOffset = Offset.Zero; onMove(0f, 0f) },
                    onDragCancel= { stickOffset = Offset.Zero; onMove(0f, 0f) },
                    onDrag      = { change, dragAmount ->
                        change.consume()
                        val newOffset = stickOffset + dragAmount
                        val dist = newOffset.getDistance()
                        stickOffset = if (dist > maxRadius)
                            newOffset * (maxRadius / dist) else newOffset
                        onMove(stickOffset.x / maxRadius, -stickOffset.y / maxRadius)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Thumb indicator
        Box(
            modifier = Modifier
                .offset { IntOffset(stickOffset.x.toInt(), stickOffset.y.toInt()) }
                .size(40.dp)
                .clip(CircleShape)
                .background(ButtonColors.STICK)
        )
    }
}

// ── Botões ABXY ────────────────────────────────────────────────
@Composable
fun AbxyButtons(
    onButtonDown: (Int) -> Unit,
    onButtonUp:   (Int) -> Unit,
    modifier:     Modifier
) {
    val buttons = listOf(
        Triple(ButtonMap.BUTTON_A, Alignment.CenterEnd,    "A" to ButtonColors.A),
        Triple(ButtonMap.BUTTON_B, Alignment.BottomCenter, "B" to ButtonColors.B),
        Triple(ButtonMap.BUTTON_X, Alignment.TopCenter,    "X" to ButtonColors.X),
        Triple(ButtonMap.BUTTON_Y, Alignment.CenterStart,  "Y" to ButtonColors.Y),
    )
    Box(modifier) {
        buttons.forEach { (btn, align, labelColor) ->
            val (label, color) = labelColor
            Box(
                modifier = Modifier
                    .align(align)
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color)
                    .pointerInput(btn) {
                        detectTapGestures(
                            onPress = {
                                onButtonDown(btn)
                                tryAwaitRelease()
                                onButtonUp(btn)
                            }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(label, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ── Botão genérico ─────────────────────────────────────────────
@Composable
fun GameButton(
    label:    String,
    color:    Color,
    onDown:   () -> Unit,
    onUp:     () -> Unit,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { onDown(); tryAwaitRelease(); onUp() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

// ── Botão pequeno (Start/Select) ───────────────────────────────
@Composable
fun SmallButton(label: String, onDown: () -> Unit, onUp: () -> Unit) {
    Box(
        modifier = Modifier
            .size(width = 56.dp, height = 24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(ButtonColors.START)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { onDown(); tryAwaitRelease(); onUp() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = 10.sp)
    }
}

// ── Botão Home ─────────────────────────────────────────────────
@Composable
fun HomeButton(onDown: () -> Unit, onUp: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(ButtonColors.HOME)
            .border(2.dp, Color.White.copy(0.3f), CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { onDown(); tryAwaitRelease(); onUp() }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text("⌂", color = Color.White, fontSize = 14.sp)
    }
}
