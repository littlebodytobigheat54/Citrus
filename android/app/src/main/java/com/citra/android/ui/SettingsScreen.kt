package com.citra.android.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.unit.*
import androidx.datastore.preferences.core.*
import com.citra.android.utils.PreferenceManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val scope   = rememberCoroutineScope()
    val prefs   = PreferenceManager

    // ── State ──────────────────────────────────────────────────
    var useVulkan          by remember { mutableStateOf(true) }
    var internalRes        by remember { mutableStateOf(2f) }
    var cpuClock           by remember { mutableStateOf(100f) }
    var useShaders         by remember { mutableStateOf(true) }
    var audioVolume        by remember { mutableStateOf(1f) }
    var darkTheme          by remember { mutableStateOf(true) }
    var showPerfOverlay    by remember { mutableStateOf(true) }
    var useHaptic          by remember { mutableStateOf(true) }
    var controlsOpacity    by remember { mutableStateOf(0.8f) }
    var controlsScale      by remember { mutableStateOf(1f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title         = { Text("Configurações") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Voltar")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier        = Modifier.fillMaxSize().padding(padding),
            contentPadding  = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {

            // ── Gráficos ────────────────────────────────────
            item { SettingsSectionHeader("🎮 Gráficos") }

            item {
                SwitchSetting(
                    title    = "Renderizador Vulkan",
                    subtitle = "Melhor desempenho em dispositivos compatíveis",
                    icon     = Icons.Default.GraphicEq,
                    checked  = useVulkan,
                    onCheckedChange = { useVulkan = it }
                )
            }

            item {
                SliderSetting(
                    title    = "Resolução Interna",
                    subtitle = "${internalRes.toInt()}x nativa",
                    icon     = Icons.Default.HighQuality,
                    value    = internalRes,
                    range    = 1f..4f,
                    steps    = 2,
                    onValueChange = { internalRes = it }
                )
            }

            item {
                SwitchSetting(
                    title    = "Shaders de Hardware",
                    subtitle = "Acelera shaders com a GPU",
                    icon     = Icons.Default.AutoAwesome,
                    checked  = useShaders,
                    onCheckedChange = { useShaders = it }
                )
            }

            // ── CPU ─────────────────────────────────────────
            item { SettingsSectionHeader("⚡ CPU") }

            item {
                SliderSetting(
                    title    = "Velocidade da CPU",
                    subtitle = "${cpuClock.toInt()}% (padrão: 100%)",
                    icon     = Icons.Default.Speed,
                    value    = cpuClock,
                    range    = 50f..400f,
                    steps    = 6,
                    onValueChange = { cpuClock = it }
                )
            }

            // ── Áudio ────────────────────────────────────────
            item { SettingsSectionHeader("🔊 Áudio") }

            item {
                SliderSetting(
                    title    = "Volume",
                    subtitle = "${(audioVolume * 100).toInt()}%",
                    icon     = Icons.Default.VolumeUp,
                    value    = audioVolume,
                    range    = 0f..1f,
                    steps    = 9,
                    onValueChange = { audioVolume = it }
                )
            }

            // ── Controles ────────────────────────────────────
            item { SettingsSectionHeader("🕹️ Controles Virtuais") }

            item {
                SliderSetting(
                    title    = "Opacidade dos Controles",
                    subtitle = "${(controlsOpacity * 100).toInt()}%",
                    icon     = Icons.Default.Opacity,
                    value    = controlsOpacity,
                    range    = 0.2f..1f,
                    steps    = 7,
                    onValueChange = { controlsOpacity = it }
                )
            }

            item {
                SliderSetting(
                    title    = "Tamanho dos Botões",
                    subtitle = "${(controlsScale * 100).toInt()}%",
                    icon     = Icons.Default.ZoomIn,
                    value    = controlsScale,
                    range    = 0.5f..1.5f,
                    steps    = 9,
                    onValueChange = { controlsScale = it }
                )
            }

            item {
                SwitchSetting(
                    title    = "Vibração Háptica",
                    subtitle = "Feedback ao pressionar botões",
                    icon     = Icons.Default.Vibration,
                    checked  = useHaptic,
                    onCheckedChange = { useHaptic = it }
                )
            }

            // ── Interface ────────────────────────────────────
            item { SettingsSectionHeader("🎨 Interface") }

            item {
                SwitchSetting(
                    title    = "Tema Escuro",
                    subtitle = "Modo noturno",
                    icon     = Icons.Default.DarkMode,
                    checked  = darkTheme,
                    onCheckedChange = { darkTheme = it }
                )
            }

            item {
                SwitchSetting(
                    title    = "Overlay de Performance",
                    subtitle = "Mostrar FPS, CPU e GPU em jogo",
                    icon     = Icons.Default.Monitor,
                    checked  = showPerfOverlay,
                    onCheckedChange = { showPerfOverlay = it }
                )
            }

            // ── Aplicar ──────────────────────────────────────
            item { Spacer(Modifier.height(16.dp)) }
            item {
                Button(
                    onClick  = {
                        scope.launch {
                            prefs.save(
                                useVulkan       = useVulkan,
                                internalRes     = internalRes.toInt(),
                                cpuClock        = cpuClock.toInt(),
                                useShaders      = useShaders,
                                audioVolume     = audioVolume,
                                controlsOpacity = controlsOpacity,
                                controlsScale   = controlsScale,
                                useHaptic       = useHaptic,
                                showPerfOverlay = showPerfOverlay
                            )
                            onBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Salvar Configurações")
                }
            }
        }
    }
}

// ── Componentes auxiliares ─────────────────────────────────────

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text  = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SwitchSetting(
    title:           String,
    subtitle:        String,
    icon:            androidx.compose.ui.graphics.vector.ImageVector,
    checked:         Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent   = { Text(title) },
        supportingContent = { Text(subtitle) },
        leadingContent    = { Icon(icon, null) },
        trailingContent   = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    )
}

@Composable
fun SliderSetting(
    title:        String,
    subtitle:     String,
    icon:         androidx.compose.ui.graphics.vector.ImageVector,
    value:        Float,
    range:        ClosedFloatingPointRange<Float>,
    steps:        Int,
    onValueChange:(Float) -> Unit
) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title,    style = MaterialTheme.typography.bodyLarge)
                Text(subtitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Slider(
            value         = value,
            onValueChange = onValueChange,
            valueRange    = range,
            steps         = steps,
            modifier      = Modifier.fillMaxWidth()
        )
    }
}
