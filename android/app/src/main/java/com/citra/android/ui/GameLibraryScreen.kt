package com.citra.android.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import com.citra.android.EmulatorActivity
import com.citra.android.model.GameEntry
import com.citra.android.utils.GameScanner
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameLibraryScreen(
    onGameSelected: (String) -> Unit,
    onOpenFile:     () -> Unit,
    onSettings:     () -> Unit
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    var games        by remember { mutableStateOf<List<GameEntry>>(emptyList()) }
    var isScanning   by remember { mutableStateOf(false) }
    var searchQuery  by remember { mutableStateOf("") }
    var showSearch   by remember { mutableStateOf(false) }

    val folderLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                isScanning = true
                games = GameScanner.scanFolder(context, it)
                isScanning = false
            }
        }
    }

    val filteredGames = remember(games, searchQuery) {
        if (searchQuery.isBlank()) games
        else games.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.region.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showSearch) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar jogos...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text("Citra")
                    }
                },
                actions = {
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(
                            if (showSearch) Icons.Default.Close else Icons.Default.Search,
                            "Buscar"
                        )
                    }
                    IconButton(onClick = { folderLauncher.launch(null) }) {
                        Icon(Icons.Default.FolderOpen, "Selecionar pasta")
                    }
                    IconButton(onClick = onOpenFile) {
                        Icon(Icons.Default.FileOpen, "Abrir arquivo")
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, "Configurações")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onOpenFile) {
                Icon(Icons.Default.Add, "Abrir jogo")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isScanning) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Procurando jogos...")
                }
            } else if (filteredGames.isEmpty()) {
                EmptyLibraryPlaceholder(
                    onAddFolder = { folderLauncher.launch(null) },
                    onOpenFile  = onOpenFile,
                    modifier    = Modifier.align(Alignment.Center)
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement   = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredGames, key = { it.path }) { game ->
                        GameCard(
                            game      = game,
                            onClick   = {
                                EmulatorActivity.launch(context, game.path)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameCard(game: GameEntry, onClick: () -> Unit) {
    Card(
        onClick   = onClick,
        modifier  = Modifier.fillMaxWidth().aspectRatio(0.75f),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape     = RoundedCornerShape(12.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            // Box art placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (game.iconPath != null) {
                    // coil image loading
                    AsyncImage(
                        model       = game.iconPath,
                        contentDescription = game.title,
                        modifier    = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.VideogameAsset,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Region badge
                Surface(
                    modifier = Modifier.align(Alignment.TopEnd).padding(6.dp),
                    color    = MaterialTheme.colorScheme.tertiaryContainer,
                    shape    = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        game.region,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style    = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Title
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    game.title,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style    = MaterialTheme.typography.bodyMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    game.fileExtension.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun EmptyLibraryPlaceholder(
    onAddFolder: () -> Unit,
    onOpenFile:  () -> Unit,
    modifier:    Modifier
) {
    Column(
        modifier  = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.SportsEsports,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "Nenhum jogo encontrado",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Adicione uma pasta com seus jogos ou abra um arquivo diretamente.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAddFolder) {
            Icon(Icons.Default.FolderOpen, null)
            Spacer(Modifier.width(8.dp))
            Text("Selecionar Pasta")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onOpenFile) {
            Icon(Icons.Default.FileOpen, null)
            Spacer(Modifier.width(8.dp))
            Text("Abrir Arquivo")
        }
    }
}
